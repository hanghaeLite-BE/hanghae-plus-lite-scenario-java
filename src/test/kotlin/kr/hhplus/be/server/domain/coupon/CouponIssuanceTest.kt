package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponIssuanceTest {
    private val baseNow: LocalDateTime = LocalDateTime.of(2025, 1, 10, 12, 0, 0)
    private val before1Hour: LocalDateTime = LocalDateTime.of(2025, 1, 10, 11, 0, 0)
    private val after1Hour: LocalDateTime = LocalDateTime.of(2025, 1, 10, 13, 0, 0)

    @Nested
    @DisplayName("issue 메서드")
    inner class IssueMethodTest {
        @Test
        @DisplayName("정상 발급 - 기간 내, 수량 미달")
        fun issue_Success_PeriodOk_QuantityOk() {
            val sut = CouponIssuance(
                id = 1L,
                couponSummaryId = 1L,
                issuedCount = 3,
                maxCount = 10,
                startAt = before1Hour,
                endAt = after1Hour
            )
            val result = sut.issue(baseNow)
            assertThat(result.issuedCount).isEqualTo(4)
            assertThat(sut.issuedCount).isEqualTo(3)
        }

        @Test
        @DisplayName("정상 발급 - 무제한 쿠폰")
        fun issue_Success_Unlimited() {
            val sut = CouponIssuance(
                id = 2L,
                couponSummaryId = 2L,
                issuedCount = 100,
                maxCount = null,
                startAt = before1Hour,
                endAt = after1Hour
            )
            val result = sut.issue(baseNow)
            assertThat(result.issuedCount).isEqualTo(101)
        }

        @Test
        @DisplayName("실패 - 발급 기간 이전")
        fun issue_Fail_BeforePeriod() {
            val sut = CouponIssuance(
                id = 3L,
                couponSummaryId = 3L,
                issuedCount = 0,
                maxCount = 10,
                startAt = baseNow.plusHours(1),
                endAt = baseNow.plusHours(2)
            )
            val ex = assertThrows<ConflictResourceException> {
                sut.issue(baseNow)
            }
            assertThat(ex.message).contains("쿠폰발급이 가능한 기간이 아닙니다.")
        }

        @Test
        @DisplayName("실패 - 발급 기간 이후")
        fun issue_Fail_AfterPeriod() {
            val sut = CouponIssuance(
                id = 4L,
                couponSummaryId = 4L,
                issuedCount = 0,
                maxCount = 10,
                startAt = baseNow.minusHours(2),
                endAt = baseNow.minusHours(1)
            )
            val ex = assertThrows<ConflictResourceException> {
                sut.issue(baseNow)
            }
            assertThat(ex.message).contains("쿠폰발급이 가능한 기간이 아닙니다.")
        }

        @Test
        @DisplayName("실패 - 수량 소진")
        fun issue_Fail_QuantityFull() {
            val sut = CouponIssuance(
                id = 5L,
                couponSummaryId = 5L,
                issuedCount = 10,
                maxCount = 10,
                startAt = before1Hour,
                endAt = after1Hour
            )
            val ex = assertThrows<ConflictResourceException> {
                sut.issue(baseNow)
            }
            assertThat(ex.message).contains("쿠폰발급수량이 부족합니다.")
        }
    }
}
