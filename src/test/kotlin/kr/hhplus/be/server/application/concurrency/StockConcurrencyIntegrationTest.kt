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
 * 재고 동시성 이슈 검증 테스트
 *
 * 이 테스트는 현재 코드의 동시성 이슈를 검증합니다.
 * - TC-STK-001: 재고 1개 상품에 2명 동시 주문 시 Overselling 검증
 * - TC-STK-002: 재고 5개 상품에 5명 동시 2개씩 주문 시 Overselling 검증
 *
 * 동시성 이슈가 해결되면 이 테스트들이 통과해야 합니다.
 */
@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(
        scripts = ["/sql/stock-concurrency-setup.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    ),
    Sql(
        scripts = ["/sql/stock-concurrency-cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    ),
)
@Import(NoOpEventPublisherConfig::class)
class StockConcurrencyIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var productSummaryJpaRepository: ProductSummaryJpaRepository

    @Autowired
    private lateinit var orderSummaryJpaRepository: OrderSummaryJpaRepository

    @Nested
    @DisplayName("TC-STK-001: 재고 1개 상품 동시 주문 테스트")
    inner class SingleStockConcurrentOrderTest {

        /**
         * 시나리오: 재고 1개인 상품을 2명이 동시에 주문
         *
         * 초기 재고: 1개
         * 사용자 A: 1개 주문
         * 사용자 B: 1개 주문
         *
         * 예상 결과 (정상): 1명 성공, 1명 실패 (재고 부족), 최종 재고 0개
         * 동시성 이슈 발생 시: 2명 모두 성공, 최종 재고 -1개 (Overselling)
         */
        @Test
        @DisplayName("재고 1개 상품에 2명 동시 주문 시 1명만 성공해야 한다")
        fun concurrentOrder_singleStock_shouldAllowOnlyOneSuccess() {
            val productId = 5001L
            val productPrice = 10000
            val initialStock = 1
            val orderQuantity = 1

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                val requests = listOf(
                    OrderRequest(
                        memberId = 4001L,
                        idempotencyKey = "stock-test-user-a",
                        productId = productId,
                        quantity = orderQuantity,
                        price = productPrice,
                    ),
                    OrderRequest(
                        memberId = 4002L,
                        idempotencyKey = "stock-test-user-b",
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
                        .filter { it.memberId in listOf(4001L, 4002L) }
                }

                assertAll(
                    {
                        assertThat(successResponses.size)
                            .describedAs("1명만 주문 성공해야 함 (Overselling 발생 시 2명 성공)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(failureResponses.size)
                            .describedAs("1명은 실패해야 함 (재고 부족)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(failureResponses.firstOrNull()?.status)
                            .describedAs("실패 응답은 409 Conflict여야 함")
                            .isEqualTo(409)
                    },
                    {
                        assertThat(finalStock)
                            .describedAs("최종 재고는 0개여야 함 (음수면 Overselling)")
                            .isEqualTo(0)
                    },
                    {
                        assertThat(finalStock)
                            .describedAs("재고가 음수가 되면 안됨")
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
    @DisplayName("TC-STK-002: 재고 5개 상품 동시 대량 주문 테스트")
    inner class MultiStockConcurrentOrderTest {

        /**
         * 시나리오: 재고 5개인 상품을 5명이 동시에 2개씩 주문
         *
         * 초기 재고: 5개
         * 사용자 C, D, E, F, G: 각각 2개 주문 (총 10개 요청)
         *
         * 예상 결과 (정상): 최대 2명 성공 (4개 판매), 3명 실패, 최종 재고 1개 이상
         * 동시성 이슈 발생 시: 3명 이상 성공, 최종 재고 음수 (심각한 Overselling)
         */
        @Test
        @DisplayName("재고 5개 상품에 5명 동시 2개씩 주문 시 최대 2명만 성공해야 한다")
        fun concurrentOrder_limitedStock_shouldPreventOverselling() {
            val productId = 5002L
            val productPrice = 10000
            val initialStock = 5
            val orderQuantity = 2

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                val requests = listOf(
                    OrderRequest(4003L, "stock-test-user-c", productId, orderQuantity, productPrice),
                    OrderRequest(4004L, "stock-test-user-d", productId, orderQuantity, productPrice),
                    OrderRequest(4005L, "stock-test-user-e", productId, orderQuantity, productPrice),
                    OrderRequest(4006L, "stock-test-user-f", productId, orderQuantity, productPrice),
                    OrderRequest(4007L, "stock-test-user-g", productId, orderQuantity, productPrice),
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
                        .filter { it.memberId in listOf(4003L, 4004L, 4005L, 4006L, 4007L) }
                }

                // 총 판매 수량 계산
                val totalSoldQuantity = successCount * orderQuantity

                assertAll(
                    {
                        assertThat(successCount)
                            .describedAs("최대 2명만 성공해야 함 (재고 5개, 주문당 2개)")
                            .isLessThanOrEqualTo(2)
                    },
                    {
                        assertThat(failureCount)
                            .describedAs("최소 3명은 실패해야 함")
                            .isGreaterThanOrEqualTo(3)
                    },
                    {
                        assertThat(finalStock)
                            .describedAs("재고가 음수가 되면 안됨 (Overselling)")
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
