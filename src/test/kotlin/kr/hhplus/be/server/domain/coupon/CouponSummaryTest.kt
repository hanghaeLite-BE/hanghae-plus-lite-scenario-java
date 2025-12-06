package kr.hhplus.be.server.domain.coupon

import io.mockk.every
import io.mockk.mockkStatic
import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.utils.StubFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.LocalDateTime

@UnitTest
class CouponSummaryTest {
    @Nested
    @DisplayName("쿠폰 만료시점 계산(현재시점: 2024-01-01 00:00:00)")
    inner class CalculateExpiredAtTest {
        private val fixedNow = LocalDateTime.of(2024, 1, 1, 0, 0)

        @BeforeEach
        fun setUp() {
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns fixedNow
        }

        @AfterEach
        fun tearDown() {
            io.mockk.unmockkStatic(LocalDateTime::class)
        }

        @Test
        @DisplayName("쿠폰 유효일이 1일인 경우 만료시점은 2024-01-02 00:00:00 이다.")
        fun expiredWhenNowAfterExpiredAt() {
            // given
            val validDays = 1
            val couponSummary = StubFactory.couponSummary(validDays = validDays)
            val issuedAt = fixedNow

            // when
            val expiredAt = couponSummary.calculateExpiredAt(issuedAt)

            // then
            assertThat(expiredAt).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0))
        }

        @Test
        @DisplayName("쿠폰 유효일이 없는 경우 만료시점은 없다")
        fun notExpiredWhenNowBeforeOrEqualExpiredAt() {
            // given
            val couponSummary = StubFactory.couponSummary(validDays = null)
            val issuedAt = fixedNow

            // when
            val expiredAt = couponSummary.calculateExpiredAt(issuedAt)

            // then
            assertThat(expiredAt).isNull()
        }
    }
}
