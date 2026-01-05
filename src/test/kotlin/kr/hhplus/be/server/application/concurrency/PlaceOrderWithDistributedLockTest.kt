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
import kr.hhplus.be.server.infrastructure.persistence.order.OrderSummaryJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.product.ProductSummaryJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc

/**
 * 분산락 적용 후 주문 동시성 테스트
 *
 * Redis 분산락이 적용된 주문 생성 로직의 동시성 제어를 검증합니다.
 * - TC-LOCK-001: 재고 1개 상품에 100명 동시 주문 시 재고 정합성 검증
 * - TC-LOCK-002: 재고 10개 상품에 20명 동시 주문 시 재고 정합성 검증
 */
@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(
        scripts = ["/sql/place-order-distributed-lock-setup.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    ),
    Sql(
        scripts = ["/sql/place-order-distributed-lock-cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    ),
)
@Import(NoOpEventPublisherConfig::class)
class PlaceOrderWithDistributedLockTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var productSummaryJpaRepository: ProductSummaryJpaRepository

    @Autowired
    private lateinit var orderSummaryJpaRepository: OrderSummaryJpaRepository

    @Nested
    @DisplayName("TC-LOCK-001: 재고 1개 상품 대량 동시 주문 테스트")
    inner class SingleStockMassiveConcurrentOrderTest {

        /**
         * 시나리오: 재고 1개인 상품을 100명이 동시에 주문
         *
         * 초기 재고: 1개
         * 동시 주문자: 100명
         * 주문 수량: 각 1개
         *
         * 예상 결과: 1명만 성공, 99명 실패, 최종 재고 0개
         * 분산락이 제대로 작동하면 재고가 음수가 되지 않아야 합니다.
         */
        @Test
        @DisplayName("재고 1개 상품에 100명 동시 주문 시 1명만 성공해야 한다")
        fun concurrentOrder_singleStock_massiveUsers_shouldAllowOnlyOneSuccess() {
            val productId = 6001L
            val productPrice = 10000
            val initialStock = 1
            val orderQuantity = 1
            val threadCount = 100

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                val requests = (1..threadCount).map { i ->
                    OrderRequest(
                        memberId = 5000L + i,
                        idempotencyKey = "lock-test-user-$i",
                        productId = productId,
                        quantity = orderQuantity,
                        price = productPrice,
                    )
                }

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
                val successCount = responses.count { it.status == 200 }
                val failureCount = responses.count { it.status != 200 }

                // DB에서 최종 재고 확인
                val finalStock = withContext(Dispatchers.IO) {
                    productSummaryJpaRepository
                        .findById(productId)
                        .orElseThrow { IllegalStateException("상품 정보를 찾을 수 없습니다.") }
                        .stockQuantity
                }

                // 생성된 주문 수 확인
                val createdOrders = withContext(Dispatchers.IO) {
                    orderSummaryJpaRepository
                        .findAll()
                        .filter { it.memberId in (5001L..5100L) }
                }

                assertAll(
                    {
                        assertThat(successCount)
                            .describedAs("1명만 주문 성공해야 함 (재고 1개)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(failureCount)
                            .describedAs("99명은 실패해야 함")
                            .isEqualTo(threadCount - 1)
                    },
                    {
                        assertThat(finalStock)
                            .describedAs("최종 재고는 0개여야 함")
                            .isEqualTo(0)
                    },
                    {
                        assertThat(finalStock)
                            .describedAs("재고가 음수가 되면 안됨 (Overselling 방지)")
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

    @Nested
    @DisplayName("TC-LOCK-002: 재고 10개 상품 대량 동시 주문 테스트")
    inner class MultiStockMassiveConcurrentOrderTest {

        /**
         * 시나리오: 재고 10개인 상품을 20명이 동시에 주문
         *
         * 초기 재고: 10개
         * 동시 주문자: 20명
         * 주문 수량: 각 1개
         *
         * 예상 결과: 최대 10명 성공, 최소 10명 실패, 최종 재고 0개
         * 분산락이 제대로 작동하면 재고가 음수가 되지 않아야 합니다.
         */
        @Test
        @DisplayName("재고 10개 상품에 20명 동시 주문 시 최대 10명만 성공해야 한다")
        fun concurrentOrder_multiStock_massiveUsers_shouldPreventOverselling() {
            val productId = 6002L
            val productPrice = 10000
            val initialStock = 10
            val orderQuantity = 1
            val threadCount = 20

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                val requests = (1..threadCount).map { i ->
                    OrderRequest(
                        memberId = 5200L + i,
                        idempotencyKey = "lock-test-user-$i",
                        productId = productId,
                        quantity = orderQuantity,
                        price = productPrice,
                    )
                }

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
                val successCount = responses.count { it.status == 200 }
                val failureCount = responses.count { it.status != 200 }

                // DB에서 최종 재고 확인
                val finalStock = withContext(Dispatchers.IO) {
                    productSummaryJpaRepository
                        .findById(productId)
                        .orElseThrow { IllegalStateException("상품 정보를 찾을 수 없습니다.") }
                        .stockQuantity
                }

                // 생성된 주문 수 확인
                val createdOrders = withContext(Dispatchers.IO) {
                    orderSummaryJpaRepository
                        .findAll()
                        .filter { it.memberId in (5201L..5220L) }
                }

                // 총 판매 수량 계산
                val totalSoldQuantity = successCount * orderQuantity

                assertAll(
                    {
                        assertThat(successCount)
                            .describedAs("최대 10명만 성공해야 함 (재고 10개)")
                            .isLessThanOrEqualTo(initialStock)
                    },
                    {
                        assertThat(failureCount)
                            .describedAs("최소 10명은 실패해야 함")
                            .isGreaterThanOrEqualTo(threadCount - initialStock)
                    },
                    {
                        assertThat(finalStock)
                            .describedAs("재고가 음수가 되면 안됨 (Overselling 방지)")
                            .isGreaterThanOrEqualTo(0)
                    },
                    {
                        assertThat(totalSoldQuantity)
                            .describedAs("총 판매 수량이 초기 재고를 초과하면 안됨")
                            .isLessThanOrEqualTo(initialStock)
                    },
                    {
                        assertThat(createdOrders.size)
                            .describedAs("주문 수는 성공 수와 일치해야 함")
                            .isEqualTo(successCount)
                    },
                    {
                        assertThat(finalStock + totalSoldQuantity)
                            .describedAs("최종 재고 + 판매 수량 = 초기 재고")
                            .isEqualTo(initialStock)
                    },
                )
            }
        }
    }

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
