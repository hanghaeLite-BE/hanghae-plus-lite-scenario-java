package kr.hhplus.be.server.presentation.web.coupon

import kr.hhplus.be.server.common.annotation.IntegrationTest
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
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(scripts = ["/sql/listing-my-coupon-setup.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/listing-my-coupon-cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class MyCouponControllerIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Nested
    @DisplayName("내 보유쿠폰 조회(GET /api/v1/coupons/me)")
    inner class GetMyCoupons {
        @Test
        @DisplayName("2개의 쿠폰(미사용 1개, 사용 1개)을 보유한 회원의 쿠폰리스트를 조회한다")
        fun getMyCoupons_success() {
            // given
            val memberId = 1L

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/coupons/me")
                        .header("X-Member-Id", memberId),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[0].name").value("SPRING_SALE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[0].discountPercentage").value(10L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[0].expiredAt").value("2025-03-21T13:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[0].usingAt").value("2025-03-20T13:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[1].id").value(2L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[1].name").value("WINTER_SALE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[1].discountPercentage").value(25L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[1].expiredAt").value("2026-01-18T13:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons[1].usingAt").value(null))
        }

        @Test
        @DisplayName("쿠폰을 보유하지 않은 회원은 빈 리스트를 조회한다")
        fun getMyCoupons_empty() {
            // given
            val memberId = 3L

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/coupons/me")
                        .header("X-Member-Id", memberId),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("$.coupons.length()").value(0))
        }
    }
}
