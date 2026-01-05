package kr.hhplus.be.server.presentation.web.order

import com.jayway.jsonpath.JsonPath
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kr.hhplus.be.server.application.port.out.CouponOutput
import kr.hhplus.be.server.application.port.out.MyBalanceOutput
import kr.hhplus.be.server.application.port.out.OrderSummaryOutput
import kr.hhplus.be.server.application.port.out.PaymentOutput
import kr.hhplus.be.server.application.port.out.ProductSummaryOutput
import kr.hhplus.be.server.common.annotation.IntegrationTest
import kr.hhplus.be.server.common.config.NoOpEventPublisherConfig
import kr.hhplus.be.server.common.support.postJsonWithIdempotency
import kr.hhplus.be.server.domain.model.payment.PaymentMethod
import kr.hhplus.be.server.utils.TransactionUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(value = ["/sql/place-order-setup.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(value = ["/sql/place-order-cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
@Import(NoOpEventPublisherConfig::class)
class PlaceOrderControllerIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberBalanceOutput: MyBalanceOutput

    @Autowired
    private lateinit var paymentOutput: PaymentOutput

    @Autowired
    private lateinit var productSummaryOutput: ProductSummaryOutput

    @Autowired
    private lateinit var couponOutput: CouponOutput

    @Autowired
    private lateinit var orderSummaryOutput: OrderSummaryOutput

    @Autowired
    private lateinit var transactionUtils: TransactionUtils

    private val fixedNow = LocalDateTime.of(2025, 6, 18, 12, 59, 59)

    @BeforeEach
    fun setUp() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedNow
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(LocalDateTime::class)
    }

    @Nested
    @DisplayName("상품주문 API(POST /api/v1/orders)")
    inner class PlaceOrderTest {
        @Test
        @DisplayName("쿠폰을 사용한 상품주문에 성공한다")
        fun placeOrder_Success() {
            // given
            val memberId = 1001L
            val balance = 2000000L
            val chargeAmount = 1712700L
            val requestBody =
                """
                {
                  "couponSummaryId": 1001,
                  "orderItems": [
                    {
                      "productSummaryId": 1001,
                      "quantity": 1,
                      "price": 1790000
                    },
                    {
                      "productSummaryId": 1002,
                      "quantity": 1,
                      "price": 89000
                    },
                    {
                      "productSummaryId": 1003,
                      "quantity": 2,
                      "price": 12000
                    }
                  ],
                  "paymentSummary": {
                    "method": "POINT",
                    "totalAmount": 1903000,
                    "discountAmount": 190300,
                    "chargeAmount": $chargeAmount
                  }
                }
                """.trimIndent()

            // when & then
            val mvcResult =
                mockMvc
                    .perform(
                        postJsonWithIdempotency(
                            uri = "/api/v1/orders",
                            body = requestBody,
                            memberId = memberId,
                        ),
                    ).andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").isNumber)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentSummary.method").value("POINT"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentSummary.totalAmount").value(1903000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentSummary.discountAmount").value(190300))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentSummary.chargeAmount").value(1712700))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems.length()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].productSummaryId").value(1001L))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].price").value(1790000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].quantity").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[1].productSummaryId").value(1002L))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[1].price").value(89000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[1].quantity").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[2].productSummaryId").value(1003L))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[2].price").value(12000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[2].quantity").value(2))
                    .andReturn()

            val responseBody = mvcResult.response.contentAsString
            val orderId = JsonPath.read<Number>(responseBody, "$.orderId").toLong()

            // 잔고 검증
            val memberBalance =
                transactionUtils.onPrimaryTransaction {
                    memberBalanceOutput.findByMemberId(memberId).get()
                }
            assertThat(memberBalance.balance).isEqualTo(balance - chargeAmount)

            // 결제정보 검증
            val payment =
                transactionUtils.onPrimaryTransaction {
                    paymentOutput.findByOrderSummaryId(orderId).get()
                }
            assertThat(payment.method).isEqualTo(PaymentMethod.POINT)
            assertThat(payment.totalAmount).isEqualTo(1903000L)
            assertThat(payment.discountAmount).isEqualTo(190300L)
            assertThat(payment.chargeAmount).isEqualTo(1712700L)

            // 상품 재고 검증
            val products =
                transactionUtils.onPrimaryTransaction {
                    productSummaryOutput
                        .findAllInIds(listOf(1001L, 1002L, 1003L))
                        .sortedBy { it.id }
                }
            assertThat(products[0].stockQuantity).isEqualTo(9) // 기존 10개
            assertThat(products[1].stockQuantity).isEqualTo(19) // 기존 20개
            assertThat(products[2].stockQuantity).isEqualTo(98) // 기존 100개

            // 쿠폰 사용 검증
            val coupon =
                transactionUtils.onPrimaryTransaction {
                    couponOutput.findByCouponSummaryIdAndMemberId(1001L, 1001L).get()
                }
            assertThat(coupon.usingAt).isEqualTo(fixedNow)

            // 주문정보 검증
            val orderSummary =
                transactionUtils.onPrimaryTransaction {
                    orderSummaryOutput.findByIdWithOrderItems(orderId).get()
                }
            assertThat(orderSummary.memberId).isEqualTo(memberId)
            assertThat(orderSummary.orderItems).hasSize(3)
            assertThat(orderSummary.orderItems).allSatisfy {
                when (it.productSummaryId) {
                    1001L -> {
                        assertThat(it.price).isEqualTo(1790000L)
                        assertThat(it.quantity).isEqualTo(1)
                    }

                    1002L -> {
                        assertThat(it.price).isEqualTo(89000L)
                        assertThat(it.quantity).isEqualTo(1)
                    }

                    1003L -> {
                        assertThat(it.price).isEqualTo(12000L)
                        assertThat(it.quantity).isEqualTo(2)
                    }

                    else -> {
                        throw IllegalArgumentException("존재하지 않는 상품입니다. productSummaryId=${it.productSummaryId}")
                    }
                }
            }
        }

        @Test
        @DisplayName("쿠폰을 사용하지않은 상품주문에 성공한다")
        fun placeOrder_WithoutCoupon_Success() {
            // given
            val memberId = 1001L
            val balance = 2000000L
            val chargeAmount = 1903000L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 1001,
                      "quantity": 1,
                      "price": 1790000
                    },
                    {
                      "productSummaryId": 1002,
                      "quantity": 1,
                      "price": 89000
                    },
                    {
                      "productSummaryId": 1003,
                      "quantity": 2,
                      "price": 12000
                    }
                  ],
                  "paymentSummary": {
                    "method": "POINT",
                    "totalAmount": 1903000,
                    "discountAmount": 0,
                    "chargeAmount": $chargeAmount
                  }
                }
                """.trimIndent()
            // when & then
            val mvcResult =
                mockMvc
                    .perform(
                        postJsonWithIdempotency(
                            uri = "/api/v1/orders",
                            body = requestBody,
                            memberId = memberId,
                        ),
                    ).andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").isNumber)
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentSummary.method").value("POINT"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentSummary.totalAmount").value(1903000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentSummary.discountAmount").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.paymentSummary.chargeAmount").value(1903000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems.length()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].productSummaryId").value(1001L))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].price").value(1790000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].quantity").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[1].productSummaryId").value(1002L))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[1].price").value(89000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[1].quantity").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[2].productSummaryId").value(1003L))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[2].price").value(12000))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[2].quantity").value(2))
                    .andReturn()

            val responseBody = mvcResult.response.contentAsString
            val orderId = JsonPath.read<Number>(responseBody, "$.orderId").toLong()

            // 잔고 검증
            val memberBalance =
                transactionUtils.onPrimaryTransaction {
                    memberBalanceOutput.findByMemberId(memberId).get()
                }
            assertThat(memberBalance.balance).isEqualTo(balance - chargeAmount)

            // 결제정보 검증
            val payment =
                transactionUtils.onPrimaryTransaction {
                    paymentOutput.findByOrderSummaryId(orderId).get()
                }
            assertThat(payment.method).isEqualTo(PaymentMethod.POINT)
            assertThat(payment.totalAmount).isEqualTo(1903000L)
            assertThat(payment.discountAmount).isEqualTo(0L)
            assertThat(payment.chargeAmount).isEqualTo(1903000L)

            // 상품 재고 검증
            val products =
                transactionUtils.onPrimaryTransaction {
                    productSummaryOutput
                        .findAllInIds(listOf(1001L, 1002L, 1003L))
                        .sortedBy { it.id }
                }
            assertThat(products[0].stockQuantity).isEqualTo(9) // 기존 10개
            assertThat(products[1].stockQuantity).isEqualTo(19) // 기존 20개
            assertThat(products[2].stockQuantity).isEqualTo(98) // 기존 100개

            // 쿠폰 사용 검증
            val coupon =
                transactionUtils.onPrimaryTransaction {
                    couponOutput.findByCouponSummaryIdAndMemberId(1001L, 1001L).get()
                }
            assertThat(coupon.usingAt).isNull()

            // 주문정보 검증
            val orderSummary =
                transactionUtils.onPrimaryTransaction {
                    orderSummaryOutput.findByIdWithOrderItems(orderId).get()
                }
            assertThat(orderSummary.memberId).isEqualTo(memberId)
            assertThat(orderSummary.orderItems).hasSize(3)
            assertThat(orderSummary.orderItems).allSatisfy {
                when (it.productSummaryId) {
                    1001L -> {
                        assertThat(it.price).isEqualTo(1790000L)
                        assertThat(it.quantity).isEqualTo(1)
                    }

                    1002L -> {
                        assertThat(it.price).isEqualTo(89000L)
                        assertThat(it.quantity).isEqualTo(1)
                    }

                    1003L -> {
                        assertThat(it.price).isEqualTo(12000L)
                        assertThat(it.quantity).isEqualTo(2)
                    }

                    else -> {
                        throw IllegalArgumentException("존재하지 않는 상품입니다. productSummaryId=${it.productSummaryId}")
                    }
                }
            }
        }

        @Test
        @DisplayName("보유하지않은 쿠폰을 사용한 상품주문에 실패한다")
        fun placeOrder_InvalidCoupon_Fail() {
            // given
            val memberId = 1001L
            val balance = 2000000L
            val requestBody =
                """
                {
                  "couponSummaryId": 99,
                  "orderItems": [
                    {
                      "productSummaryId": 1001,
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
                """.trimIndent()

            // when & then
            mockMvc
                .perform(
                    postJsonWithIdempotency(
                        uri = "/api/v1/orders",
                        body = requestBody,
                        memberId = memberId,
                    ),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("보유하지 않은 쿠폰입니다."))

            // 잔고유지 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance)
        }

        @Test
        @DisplayName("결제수단이 지원하지않는 경우 주문에 실패한다")
        fun placeOrder_UnsupportedPaymentMethod_Fail() {
            // given
            val memberId = 1001L
            val balance = 2000000L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 1001,
                      "quantity": 1,
                      "price": 1790000
                    }
                  ],
                  "paymentSummary": {
                    "method": "CASH",
                    "totalAmount": 1790000,
                    "discountAmount": 0,
                    "chargeAmount": 1790000
                  }
                }
                """.trimIndent()
            // when & then
            mockMvc
                .perform(
                    postJsonWithIdempotency(
                        uri = "/api/v1/orders",
                        body = requestBody,
                        memberId = memberId,
                    ),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("지원하지않는 결제수단입니다."))

            // 잔고유지 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance)
        }

        @Test
        @DisplayName("잔고가 부족할 경우 주문에 실패한다")
        fun placeOrder_InsufficientBalance_Fail() {
            // given
            val memberId = 1001L
            val balance = 2000000L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 1001,
                      "quantity": 2,
                      "price": 1790000
                    }
                  ],
                  "paymentSummary": {
                    "method": "POINT",
                    "totalAmount": 3580000,
                    "discountAmount": 0,
                    "chargeAmount": 3580000
                  }
                }
                """.trimIndent()

            // when & then
            mockMvc
                .perform(
                    postJsonWithIdempotency(
                        uri = "/api/v1/orders",
                        body = requestBody,
                        memberId = memberId,
                    ),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("잔고의 금액이 부족합니다."))

            // 잔고유지 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance)
        }

        @Test
        @DisplayName("상품의 재고가 부족할 경우 주문에 실패한다")
        fun placeOrder_InsufficientProductQuantity_Fail() {
            // given
            val memberId = 1001L
            val balance = 2000000L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 1003,
                      "quantity": 101,
                      "price": 12000
                    }
                  ],
                  "paymentSummary": {
                    "method": "POINT",
                    "totalAmount": 132000,
                    "discountAmount": 0,
                    "chargeAmount": 132000
                  }
                }
                """.trimIndent()

            // when & then
            mockMvc
                .perform(
                    postJsonWithIdempotency(
                        uri = "/api/v1/orders",
                        body = requestBody,
                        memberId = memberId,
                    ),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect(
                    MockMvcResultMatchers
                        .jsonPath("$.message")
                        .value("'신지모루 액정필름'의 재고가 부족합니다."),
                )

            // 잔고유지 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance)
        }

        @Test
        @DisplayName("동일한 Idempotency-Key로 재요청 시 캐싱된 응답을 반환한다")
        fun placeOrder_ReplayedRequest_ReturnsCachedResponse() {
            val memberId = 1001L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 1001,
                      "quantity": 1,
                      "price": 1790000
                    }
                  ],
                  "paymentSummary": {
                    "method": "POINT",
                    "totalAmount": 1790000,
                    "discountAmount": 0,
                    "chargeAmount": 1790000
                  }
                }
                """.trimIndent()

            val idempotencyKey = UUID.randomUUID().toString()

            val firstResponse =
                mockMvc
                    .perform(
                        postJsonWithIdempotency(
                            uri = "/api/v1/orders",
                            body = requestBody,
                            memberId = memberId,
                            idempotencyKey = idempotencyKey,
                        ),
                    ).andExpect(MockMvcResultMatchers.status().isOk)
                    .andReturn()
                    .response
                    .contentAsString

            val secondResponse =
                mockMvc
                    .perform(
                        postJsonWithIdempotency(
                            uri = "/api/v1/orders",
                            body = requestBody,
                            memberId = memberId,
                            idempotencyKey = idempotencyKey,
                        ),
                    ).andExpect(MockMvcResultMatchers.status().isOk)
                    .andReturn()
                    .response
                    .contentAsString

            assertThat(secondResponse).isEqualTo(firstResponse)
        }
    }
}
