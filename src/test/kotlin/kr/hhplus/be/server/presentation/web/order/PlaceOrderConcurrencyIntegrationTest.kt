package kr.hhplus.be.server.presentation.web.order

import com.jayway.jsonpath.JsonPath
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kr.hhplus.be.server.common.annotation.IntegrationTest
import kr.hhplus.be.server.common.config.NoOpEventPublisherConfig
import kr.hhplus.be.server.common.support.postJsonWithIdempotency
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDateTime

@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(
        scripts = ["/sql/place-order-concurrency-setup.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    ),
    Sql(
        scripts = ["/sql/place-order-concurrency-cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    ),
)
@Import(NoOpEventPublisherConfig::class)
class PlaceOrderConcurrencyIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private val fixedNow = LocalDateTime.of(2025, 10, 1, 0, 0, 0)

    @BeforeEach
    fun setUp() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedNow
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(LocalDateTime::class)
    }

    @Test
    @DisplayName("1개의 쿠폰을 사용하여 서로 다른 상품주문 요청하면 하나의 주문은 이미 사용된 쿠폰입니다란 메세지로 실패한다")
    fun placeOrder_concurrency_singleCoupon_allowsOnlyOneSuccess() {
        runBlocking {
            val startSignal = CompletableDeferred<Unit>()
            val scenarios =
                listOf(
                    ConcurrencyOrderRequest(
                        memberId = 2004L,
                        idempotencyKey = "order-concurrency-request-1",
                        requestBody =
                            """
                            {
                              "couponSummaryId": 200,
                              "orderItems": [
                                {
                                  "productSummaryId": 9001,
                                  "quantity": 1,
                                  "price": 1790000
                                }
                              ],
                              "paymentSummary": {
                                "method": "POINT",
                                "totalAmount": 1790000,
                                "discountAmount": 179000,
                                "chargeAmount": 1611000
                              }
                            }
                            """.trimIndent(),
                    ),
                    ConcurrencyOrderRequest(
                        memberId = 2004L,
                        idempotencyKey = "order-concurrency-request-2",
                        requestBody =
                            """
                            {
                              "couponSummaryId": 200,
                              "orderItems": [
                                {
                                  "productSummaryId": 9002,
                                  "quantity": 1,
                                  "price": 99000
                                }
                              ],
                              "paymentSummary": {
                                "method": "POINT",
                                "totalAmount": 99000,
                                "discountAmount": 9900,
                                "chargeAmount": 89100
                              }
                            }
                            """.trimIndent(),
                    ),
                )

            val jobs =
                scenarios.map { scenario ->
                    async(Dispatchers.IO) {
                        startSignal.await()
                        mockMvc
                            .perform(
                                postJsonWithIdempotency(
                                    uri = "/api/v1/orders",
                                    body = scenario.requestBody,
                                    memberId = scenario.memberId,
                                    idempotencyKey = scenario.idempotencyKey,
                                ),
                            ).andReturn()
                            .response
                    }
                }

            startSignal.complete(Unit)
            val responses = jobs.awaitAll()

            responses
                .partition { it.status == 200 }
                .also { (success, failure) ->
                    assertAll(
                        { assertThat(success).describedAs("성공 응답").hasSize(1) },
                        { assertThat(failure).describedAs("실패 응답").hasSize(1) },
                        { assertThat(failure[0].status).isEqualTo(409) },
                        {
                            val message = JsonPath.read<String>(failure[0].contentAsString, "$.message")
                            assertThat(message).contains("이미 사용되었거나 만료된 쿠폰입니다.")
                        },
                    )
                }
        }
    }

    private data class ConcurrencyOrderRequest(
        val memberId: Long,
        val idempotencyKey: String,
        val requestBody: String,
    )
}
