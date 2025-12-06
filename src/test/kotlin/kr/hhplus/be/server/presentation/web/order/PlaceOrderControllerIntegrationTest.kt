package kr.hhplus.be.server.presentation.web.order

import io.mockk.every
import io.mockk.mockkStatic
import kr.hhplus.be.server.application.port.out.MyBalanceOutput
import kr.hhplus.be.server.common.annotation.IntegrationTest
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
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(value = ["/sql/place-order-setup.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(value = ["/sql/place-order-cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class PlaceOrderControllerIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberBalanceOutput: MyBalanceOutput

    private val fixedNow = LocalDateTime.of(2025, 6, 18, 12, 59, 59)

    @BeforeEach
    fun setUp() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedNow
    }

    @AfterEach
    fun tearDown() {
        io.mockk.unmockkStatic(LocalDateTime::class)
    }

    @Nested
    @DisplayName("상품주문 API(POST /api/v1/orders)")
    inner class PlaceOrderTest {
        @Test
        @DisplayName("쿠폰을 사용한 상품주문에 성공한다")
        fun placeOrder_Success() {
            // given
            val memberId = 4L
            val balance = 2000000L
            val chargeAmount = 1712700L
            val requestBody =
                """
                {
                  "couponSummaryId": 1,
                  "orderItems": [
                    {
                      "productSummaryId": 1,
                      "quantity": 1,
                      "price": 1790000
                    },
                    {
                      "productSummaryId": 2,
                      "quantity": 1,
                      "price": 89000
                    },
                    {
                      "productSummaryId": 3,
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
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/orders")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderId").value(1L) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.paymentSummary.method").value("POINT") }
                .andExpect { MockMvcResultMatchers.jsonPath("$.paymentSummary.totalAmount").value(1909000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.paymentSummary.discountAmount").value(190900) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.paymentSummary.chargeAmount").value(1718100) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems.length()").value(3) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[0].productSummaryId").value(1L) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[0].price").value(1790000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[0].quantity").value(1) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[1].productSummaryId").value(2L) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[1].price").value(89000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[1].quantity").value(1) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[2].productSummaryId").value(3L) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[2].price").value(12000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[2].quantity").value(2) }

            // 잔고 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance - chargeAmount)
        }

        @Test
        @DisplayName("쿠폰을 사용하지않은 상품주문에 성공한다")
        fun placeOrder_WithoutCoupon_Success() {
            // given
            val memberId = 4L
            val balance = 2000000L
            val chargeAmount = 1903000L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 1,
                      "quantity": 1,
                      "price": 1790000
                    },
                    {
                      "productSummaryId": 2,
                      "quantity": 1,
                      "price": 89000
                    },
                    {
                      "productSummaryId": 3,
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
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/orders")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderId").value(1L) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.paymentSummary.method").value("POINT") }
                .andExpect { MockMvcResultMatchers.jsonPath("$.paymentSummary.totalAmount").value(1909000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.paymentSummary.discountAmount").value(0) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.paymentSummary.chargeAmount").value(1909000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems.length()").value(3) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[0].productSummaryId").value(1L) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[0].price").value(1790000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[0].quantity").value(1) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[1].productSummaryId").value(2L) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[1].price").value(89000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[1].quantity").value(1) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[2].productSummaryId").value(3L) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[2].price").value(12000) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.orderItems[2].quantity").value(2) }

            // 잔고 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance - chargeAmount)
        }

        @Test
        @DisplayName("보유하지않은 쿠폰을 사용한 상품주문에 실패한다")
        fun placeOrder_InvalidCoupon_Fail() {
            // given
            val memberId = 4L
            val balance = 2000000L
            val requestBody =
                """
                {
                  "couponSummaryId": 99,
                  "orderItems": [
                    {
                      "productSummaryId": 1,
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
                    MockMvcRequestBuilders
                        .post("/api/v1/orders")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect { MockMvcResultMatchers.jsonPath("$.message").value("보유하지 않은 쿠폰입니다.") }

            // 잔고유지 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance)
        }

        @Test
        @DisplayName("결제수단이 지원하지않는 경우 주문에 실패한다")
        fun placeOrder_UnsupportedPaymentMethod_Fail() {
            // given
            val memberId = 4L
            val balance = 2000000L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 1,
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
                    MockMvcRequestBuilders
                        .post("/api/v1/orders")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect { MockMvcResultMatchers.jsonPath("$.message").value("지원하지 않는 결제수단입니다.") }

            // 잔고유지 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance)
        }

        @Test
        @DisplayName("잔고가 부족할 경우 주문에 실패한다")
        fun placeOrder_InsufficientBalance_Fail() {
            // given
            val memberId = 4L
            val balance = 2000000L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 1,
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
                    MockMvcRequestBuilders
                        .post("/api/v1/orders")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect { MockMvcResultMatchers.jsonPath("$.message").value("잔고의 잔액이 부족합니다.") }

            // 잔고유지 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance)
        }

        @Test
        @DisplayName("상품의 재고가 부족할 경우 주문에 실패한다")
        fun placeOrder_InsufficientProductQuantity_Fail() {
            // given
            val memberId = 4L
            val balance = 2000000L
            val requestBody =
                """
                {
                  "orderItems": [
                    {
                      "productSummaryId": 3,
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
                    MockMvcRequestBuilders
                        .post("/api/v1/orders")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect { MockMvcResultMatchers.jsonPath("$.message").value("'신지모루 액정필름'의 재고가 부족합니다.") }

            // 잔고유지 검증
            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(balance)
        }
    }
}
