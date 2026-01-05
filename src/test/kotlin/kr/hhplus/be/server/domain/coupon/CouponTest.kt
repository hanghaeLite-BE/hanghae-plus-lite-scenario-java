package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.utils.StubFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

@UnitTest
class CouponTest {
    @Nested
    @DisplayName("할인금액 계산")
    inner class CalculateDiscountedPriceTest {
        @Test
        @DisplayName("할인율이 25%인 쿠폰으로 금액 4000원에 적용하면 1000원이 할인된다.")
        fun calculateDiscountedPriceApplies25PercentDiscount() {
            // given
            val couponSummary = StubFactory.couponSummary(
                id = 1L,
                name = "[블랙프라이데이] 25% 할인 쿠폰",
                discountPercentage = 25L,
                validDays = 30,
            )
            val coupon = StubFactory.coupon(
                id = 1L,
                couponSummary = couponSummary,
                memberId = 1L,
                usingAt = null,
                now = LocalDateTime.of(2024, 11, 1, 0, 0),
            )

            // when
            val discountedPrice = coupon.calculateDiscountAmount(4000L)

            // then
            assertThat(discountedPrice).isEqualTo(1000L)
        }
    }

    @Nested
    @DisplayName("쿠폰 사용")
    inner class UsingTest {
        @Test
        @DisplayName("정상: 사용하지 않았고 만료되지 않은 쿠폰을 사용하면 usingAt이 now로 변경된다")
        fun useCoupon_success() {
            // given
            val issuedAt = LocalDateTime.of(2024, 1, 1, 0, 0)
            val couponSummary = StubFactory.couponSummary(validDays = 2)
            val coupon = StubFactory.coupon(
                couponSummary = couponSummary,
                memberId = 1L,
                usingAt = null,
                now = issuedAt
            )

            // when
            val useTime = issuedAt.plusDays(1)
            val usedCoupon = coupon.using(useTime)

            // then
            assertThat(usedCoupon.usingAt).isEqualTo(useTime)
        }

        @Test
        @DisplayName("실패: 이미 사용된 쿠폰을 사용하려 하면 예외 발생")
        fun useCoupon_alreadyUsed_throws() {
            // given
            val issuedAt = LocalDateTime.of(2024, 1, 1, 0, 0)
            val couponSummary = StubFactory.couponSummary(validDays = 2)
            val coupon = StubFactory.coupon(
                couponSummary = couponSummary,
                memberId = 1L,
                usingAt = issuedAt.plusDays(1),
                now = issuedAt
            )

            // when
            val useTime = issuedAt.plusDays(2)
            val ex = assertThrows<ConflictResourceException> {
                coupon.using(useTime)
            }

            // then
            assertThat(ex.message).contains("이미 사용된 쿠폰입니다.")
        }

        @Test
        @DisplayName("실패: 만료된 쿠폰을 사용하려 하면 예외 발생")
        fun useCoupon_expired_throws() {
            // given
            val issuedAt = LocalDateTime.of(2024, 1, 1, 0, 0)
            val couponSummary = StubFactory.couponSummary(validDays = 1)
            val coupon = StubFactory.coupon(
                couponSummary = couponSummary,
                memberId = 1L,
                usingAt = null,
                now = issuedAt
            )

            // when
            val useTime = issuedAt.plusDays(2)
            val ex = assertThrows<ConflictResourceException> {
                coupon.using(useTime)
            }

            // then
            assertThat(ex.message).contains("만료된 쿠폰입니다.")
        }
    }

    @Nested
    @DisplayName("쿠폰 생성")
    inner class DomainMethodTest {
        @Test
        fun `create 메서드는 초기값이 올바른 Coupon을 반환한다`() {
            // given
            val couponSummary = StubFactory.couponSummary(validDays = 5)
            val now = LocalDateTime.of(2024, 1, 1, 0, 0)

            // when
            val coupon = Coupon.create(
                memberId = 1L,
                couponSummary = couponSummary,
                now = now
            )

            // then
            assertThat(coupon.id).isNull()
            assertThat(coupon.memberId).isEqualTo(1L)
            assertThat(coupon.couponSummary).isEqualTo(couponSummary)
            assertThat(coupon.usingAt).isNull()
            assertThat(coupon.expiredAt).isEqualTo(couponSummary.calculateExpiredAt(now))
        }
    }
}
