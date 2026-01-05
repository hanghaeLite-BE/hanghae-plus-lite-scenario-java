package kr.hhplus.be.server.presentation.web.coupon

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.transaction.Transactional
import kr.hhplus.be.server.common.annotation.IntegrationTest
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime

@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(scripts = ["/sql/coupon-issuance-setup.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/coupon-issuance-cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class CouponIssuanceControllerIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val fixedNow = LocalDateTime.of(2025, 9, 24, 0, 0, 0)

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
    @DisplayName("쿠폰 발급(POST /api/v1/coupons/issuance)")
    inner class IssueCouponTest {
        @Transactional
        @Test
        @DisplayName("정상 발급: 기간 내, 수량 미달, 정책 통과")
        fun issueCoupon_success() {
            val memberId = 1L
            val couponSummaryId = 7001L // 정상쿠폰
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/coupons/issuance")
                        .header("X-Member-Id", memberId)
                        .header("Idempotency-Key", "issue-success")
                        .content("{\"couponSummaryId\": $couponSummaryId}")
                        .contentType("application/json"),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.couponId").exists())
        }

        @Transactional
        @Test
        @DisplayName("같은 Idempotency-Key로 중복 요청하면 캐시된 응답을 반환한다")
        fun issueCoupon_idempotent() {
            val memberId = 1L
            val couponSummaryId = 7001L
            val idempotencyKey = "issue-idempotent"

            val firstResponse =
                mockMvc
                    .perform(
                        MockMvcRequestBuilders
                            .post("/api/v1/coupons/issuance")
                            .header("X-Member-Id", memberId)
                            .header("Idempotency-Key", idempotencyKey)
                            .content("{\"couponSummaryId\": $couponSummaryId}")
                            .contentType("application/json"),
                    ).andExpect(MockMvcResultMatchers.status().isOk)
                    .andReturn()

            val cachedCouponId =
                objectMapper
                    .readTree(firstResponse.response.contentAsString)["couponId"]
                    .asLong()

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/coupons/issuance")
                        .header("X-Member-Id", memberId)
                        .header("Idempotency-Key", idempotencyKey)
                        .content("{\"couponSummaryId\": $couponSummaryId}")
                        .contentType("application/json"),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.couponId").value(cachedCouponId))
        }

        @Test
        @DisplayName("실패: 발급 기간이 아님")
        fun issueCoupon_fail_period() {
            val memberId = 1L
            val couponSummaryId = 7002L // 기간외쿠폰
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/coupons/issuance")
                        .header("X-Member-Id", memberId)
                        .header("Idempotency-Key", "issue-fail-period")
                        .content("{\"couponSummaryId\": $couponSummaryId}")
                        .contentType("application/json"),
                ).andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("쿠폰 발급에 실패했습니다. 수량이 소진되었거나 발급 기간이 아닙니다."))
        }

        @Test
        @DisplayName("실패: 수량 소진")
        fun issueCoupon_fail_maxCount() {
            val memberId = 1L
            val couponSummaryId = 7003L // 수량소진쿠폰
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/coupons/issuance")
                        .header("X-Member-Id", memberId)
                        .header("Idempotency-Key", "issue-fail-max")
                        .content("{\"couponSummaryId\": $couponSummaryId}")
                        .contentType("application/json"),
                ).andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("쿠폰 발급에 실패했습니다. 수량이 소진되었거나 발급 기간이 아닙니다."))
        }

        @Test
        @DisplayName("실패: 정책 위반(중복 발급 등)")
        fun issueCoupon_fail_policy() {
            val memberId = 7002L // 이미 발급된 회원
            val couponSummaryId = 7001L // 정상쿠폰
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/coupons/issuance")
                        .header("X-Member-Id", memberId)
                        .header("Idempotency-Key", "issue-fail-policy")
                        .content("{\"couponSummaryId\": $couponSummaryId}")
                        .contentType("application/json"),
                ).andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("중복발급이 제한된 쿠폰입니다."))
        }
    }
}
