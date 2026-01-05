package kr.hhplus.be.server.application.concurrency

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.hhplus.be.server.common.annotation.IntegrationTest
import kr.hhplus.be.server.common.config.NoOpEventPublisherConfig
import kr.hhplus.be.server.common.support.postJsonWithIdempotency
import kr.hhplus.be.server.infrastructure.persistence.balance.MemberBalanceJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.order.OrderSummaryJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

/**
 * 잔액 동시성 이슈 검증 테스트
 *
 * 이 테스트는 현재 코드의 동시성 이슈를 검증합니다.
 * - TC-BAL-001: 동시 충전 시 Lost Update 검증 (잔액 충전 API)
 * - TC-BAL-002: 동시 주문 시 잔액 차감 Race Condition 검증 (주문 API)
 *
 * 동시성 이슈가 해결되면 이 테스트들이 통과해야 합니다.
 */
@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(
        scripts = ["/sql/balance-concurrency-setup.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    ),
    Sql(
        scripts = ["/sql/balance-concurrency-cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    ),
)
@Import(NoOpEventPublisherConfig::class)
class BalanceConcurrencyIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberBalanceJpaRepository: MemberBalanceJpaRepository

    @Autowired
    private lateinit var orderSummaryJpaRepository: OrderSummaryJpaRepository

    @Nested
    @DisplayName("TC-BAL-001: 잔액 충전 API - 동시 충전 시 Lost Update 검증")
    inner class ConcurrentRechargeTest {

        /**
         * 검증 대상: MemberBalanceService.recharge() 메서드의 동시성 처리
         *
         * 시나리오: 같은 사용자가 동시에 2번의 잔액 충전 요청
         *
         * 초기 잔액: 10,000원
         * 요청1: 5,000원 충전
         * 요청2: 3,000원 충전
         *
         * 예상 결과 (정상): 최종 잔액 18,000원
         * 동시성 이슈 발생 시: 최종 잔액 13,000원 또는 15,000원 (Lost Update)
         *
         * 발생 원인: 두 트랜잭션이 동시에 같은 잔액(10,000원)을 읽고,
         *          각각 계산한 결과를 덮어쓰면서 한 요청의 충전 금액이 유실됨
         */
        @Test
        @DisplayName("동시에 잔액 충전 요청 시 모든 금액이 정상 반영되어야 한다")
        fun concurrentRecharge_shouldApplyAllAmounts() {
            val memberId = 3001L
            val memberBalanceId = 3001L
            val initialBalance = 10000L
            val rechargeAmount1 = 5000L
            val rechargeAmount2 = 3000L
            val expectedFinalBalance = initialBalance + rechargeAmount1 + rechargeAmount2 // 18,000원

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                val requests = listOf(
                    RechargeRequest(memberId, memberBalanceId, rechargeAmount1),
                    RechargeRequest(memberId, memberBalanceId, rechargeAmount2),
                )

                val jobs = requests.map { request ->
                    async(Dispatchers.IO) {
                        startSignal.await()
                        mockMvc
                            .perform(
                                MockMvcRequestBuilders
                                    .put("/api/v1/balances/me/${request.memberBalanceId}/recharge")
                                    .header("X-Member-Id", request.memberId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""{"chargeAmount": ${request.amount}}"""),
                            )
                            .andReturn()
                            .response
                    }
                }

                startSignal.complete(Unit)
                val responses = jobs.awaitAll()

                // 두 요청 모두 성공해야 함
                val successCount = responses.count { it.status == 200 }

                // DB에서 최종 잔액 확인
                val finalBalance = withContext(Dispatchers.IO) {
                    memberBalanceJpaRepository
                        .findById(memberBalanceId)
                        .orElseThrow { IllegalStateException("잔액 정보를 찾을 수 없습니다.") }
                        .balance
                }

                assertAll(
                    { assertThat(successCount).describedAs("두 요청 모두 성공해야 함").isEqualTo(2) },
                    {
                        assertThat(finalBalance)
                            .describedAs(
                                "최종 잔액이 $expectedFinalBalance 원이어야 함 (Lost Update 발생 시 실패)",
                            )
                            .isEqualTo(expectedFinalBalance)
                    },
                )
            }
        }
    }

    @Nested
    @DisplayName("TC-BAL-002: 주문 API - 동시 주문 시 잔액 차감 Race Condition 검증")
    inner class ConcurrentOrderBalanceDeductionTest {

        /**
         * 검증 대상: PlaceOrderFacade 내 잔액 차감 로직의 동시성 처리
         *
         * 시나리오: 동일 사용자가 동시에 2건의 주문 요청 (잔액 부족 상황)
         *
         * 초기 잔액: 15,000원
         * 주문1: 10,000원 상품 주문
         * 주문2: 10,000원 상품 주문 (다른 idempotency key)
         *
         * 예상 결과 (정상): 1건 성공, 1건 실패 (잔액 부족), 최종 잔액 5,000원
         * 동시성 이슈 발생 시: 2건 모두 성공, 최종 잔액 -5,000원 (음수 잔액)
         *
         * 발생 원인: 두 트랜잭션이 동시에 잔액(15,000원)을 읽고,
         *          각각 잔액 충분으로 판단하여 차감을 진행함
         *
         * NOTE: 재고가 아닌 '잔액'이 제한 요소가 되도록 재고는 충분히 설정함 (100개)
         */
        @Test
        @DisplayName("동시에 주문 요청 시 잔액 부족하면 하나만 성공해야 한다")
        fun concurrentOrder_shouldAllowOnlyOneSuccess_whenInsufficientBalance() {
            val memberId = 3002L
            val memberBalanceId = 3002L
            val productId = 3001L
            val productPrice = 10000
            val initialBalance = 15000L
            val orderQuantity = 1

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                // 동일 사용자가 서로 다른 idempotency key로 2건의 주문 요청
                val requests = listOf(
                    OrderRequest(
                        memberId = memberId,
                        idempotencyKey = "balance-test-order-1",
                        productId = productId,
                        quantity = orderQuantity,
                        price = productPrice,
                    ),
                    OrderRequest(
                        memberId = memberId,
                        idempotencyKey = "balance-test-order-2",
                        productId = productId,
                        quantity = orderQuantity,
                        price = productPrice,
                    ),
                )

                val jobs = requests.map { request ->
                    async(Dispatchers.IO) {
                        startSignal.await()
                        mockMvc
                            .perform(
                                postJsonWithIdempotency(
                                    uri = "/api/v1/orders",
                                    body = request.toJson(),
                                    memberId = request.memberId,
                                    idempotencyKey = request.idempotencyKey,
                                ),
                            )
                            .andReturn()
                            .response
                    }
                }

                startSignal.complete(Unit)
                val responses = jobs.awaitAll()

                // 응답 분석
                val successResponses = responses.filter { it.status == 200 }
                val failureResponses = responses.filter { it.status != 200 }

                // DB에서 최종 잔액 확인
                val finalBalance = withContext(Dispatchers.IO) {
                    memberBalanceJpaRepository
                        .findById(memberBalanceId)
                        .orElseThrow { IllegalStateException("잔액 정보를 찾을 수 없습니다.") }
                        .balance
                }

                // 생성된 주문 수 확인
                val createdOrders = withContext(Dispatchers.IO) {
                    orderSummaryJpaRepository
                        .findAll()
                        .filter { it.memberId == memberId }
                }

                assertAll(
                    {
                        assertThat(successResponses.size)
                            .describedAs("1건만 주문 성공해야 함 (잔액 부족으로 인해)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(failureResponses.size)
                            .describedAs("1건은 실패해야 함 (잔액 부족)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(finalBalance)
                            .describedAs("최종 잔액은 5,000원이어야 함 (15,000 - 10,000)")
                            .isEqualTo(initialBalance - productPrice)
                    },
                    {
                        assertThat(finalBalance)
                            .describedAs("잔액이 음수가 되면 안됨 (Race Condition 발생 시 실패)")
                            .isGreaterThanOrEqualTo(0)
                    },
                    {
                        assertThat(createdOrders.size)
                            .describedAs("주문은 1건만 생성되어야 함")
                            .isEqualTo(1)
                    },
                )
            }
        }
    }

    private data class RechargeRequest(
        val memberId: Long,
        val memberBalanceId: Long,
        val amount: Long,
    )

    private data class OrderRequest(
        val memberId: Long,
        val idempotencyKey: String,
        val productId: Long,
        val quantity: Int,
        val price: Int,
    ) {
        fun toJson(): String {
            val totalAmount = price * quantity
            return """
                {
                  "couponSummaryId": null,
                  "orderItems": [
                    {
                      "productSummaryId": $productId,
                      "quantity": $quantity,
                      "price": $price
                    }
                  ],
                  "paymentSummary": {
                    "method": "POINT",
                    "totalAmount": $totalAmount,
                    "discountAmount": 0,
                    "chargeAmount": $totalAmount
                  }
                }
            """.trimIndent()
        }
    }
}
