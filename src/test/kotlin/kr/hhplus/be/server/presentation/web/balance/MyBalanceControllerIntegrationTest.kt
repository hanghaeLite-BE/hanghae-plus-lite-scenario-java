package kr.hhplus.be.server.presentation.web.balance

import kr.hhplus.be.server.application.port.out.MyBalanceOutput
import kr.hhplus.be.server.common.annotation.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class MyBalanceControllerIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberBalanceOutput: MyBalanceOutput

    @Nested
    @DisplayName("내 잔고 조회(GET /api/v1/balances/me)")
    inner class GetMyBalance {
        @ParameterizedTest
        @CsvSource(
            value = [
                "1, 1, 10000",
                "2, 2, 25000",
                "3, 3, 15000",
            ],
        )
        @DisplayName("존재하는 회원의 잔고를 조회한다")
        fun getMyBalance_success(
            memberId: Long,
            balanceId: Long,
            availableAmount: Long,
        ) {
            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/balances/me")
                        .header("X-Member-Id", memberId),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.balanceId").value(balanceId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.availableAmount").value(availableAmount))

            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(availableAmount)
        }

        @Test
        @DisplayName("존재하지 않는 회원의 잔고 조회 시 404 Not Found를 반환한다")
        fun not_exist_member_balance() {
            // given
            val memberId = 999L

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/balances/me")
                        .header("X-Member-Id", memberId),
                ).andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect { MockMvcResultMatchers.jsonPath("$.message").value("잔고정보를 찾을 수 없습니다.") }
        }

        @Test
        @DisplayName("X-Member-Id 헤더가 없을 경우 400 Bad Request를 반환한다")
        fun getMyBalance_missingHeader() {
            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/api/v1/balances/me"),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("내 잔고 충전(PUT /api/v1/balances/me/{memberBalanceId}/recharge)")
    inner class RechargeMyBalance {
        @ParameterizedTest
        @CsvSource(
            value = [
                "1, 1, 10000",
                "2, 2, 25000",
                "3, 3, 15000",
            ],
        )
        @DisplayName("존재하는 잔고에 요청한 금액만큼 잔액을 충전한다")
        fun rechargeMyBalance_success(
            memberId: Long,
            balanceId: Long,
            balance: Long,
        ) {
            // given
            val chargeAmount = 5000
            val requestBody = "{\"chargeAmount\":$chargeAmount}"
            val rechargedAmount = balance + chargeAmount

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .put("/api/v1/balances/me/$balanceId/recharge")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect { MockMvcResultMatchers.jsonPath("$.balanceId").value(balanceId) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.availableAmount").value(rechargedAmount) }

            val memberBalance = memberBalanceOutput.findByMemberId(memberId).get()
            assertThat(memberBalance.balance).isEqualTo(rechargedAmount)
        }

        @Test
        @DisplayName("존재하지않는 memberId로 요청 시 404 Not Found를 반환한다")
        fun rechargeMyBalance_invalidMemberId() {
            // given
            val invalidMemberId = 999L
            val invalidBalanceId = 1L
            val requestBody = "{\"chargeAmount\":5000}"

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .put("/api/v1/balances/me/$invalidBalanceId/recharge")
                        .header("X-Member-Id", invalidMemberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect { MockMvcResultMatchers.jsonPath("$.message").value("잔고정보를 찾을 수 없습니다.") }
        }

        @Test
        @DisplayName("잘못된 memberBalanceId로 요청 시 404 Not Found를 반환한다")
        fun rechargeMyBalance_invalidBalanceId() {
            // given
            val memberId = 1L
            val invalidBalanceId = 2L
            val requestBody = "{\"chargeAmount\":5000}"

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .put("/api/v1/balances/me/$invalidBalanceId/recharge")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect { MockMvcResultMatchers.jsonPath("$.message").value("잔고정보를 찾을 수 없습니다.") }
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "-1000", "-5000"])
        @DisplayName("충전요청 금액이 0원이거나 더 작을 경우 400 Bad Request를 반환한다")
        fun rechargeMyBalance_invalidChargeAmount(chargeAmount: Int) {
            // given
            val memberId = 1L
            val balanceId = 1L
            val requestBody = "{\"chargeAmount\":$chargeAmount}"

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .put("/api/v1/balances/me/$balanceId/recharge")
                        .header("X-Member-Id", memberId)
                        .contentType("application/json")
                        .content(requestBody),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect { MockMvcResultMatchers.jsonPath("$.message").value("충전 금액은 0원보다 커야합니다.") }
        }
    }
}
