package kr.hhplus.be.server.application.interactor.coupon

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.port.out.CouponOutput
import kr.hhplus.be.server.application.vo.CouponItemVO
import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import kr.hhplus.be.server.domain.utils.StubFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@UnitTest
class MyCouponInteractorTest {
    private val couponOutput: CouponOutput = mockk()
    private val interactor = MyCouponInteractor(couponOutput)

    @Nested
    @DisplayName("내 쿠폰 조회")
    inner class GetMyCouponsTest {
        @Test
        @DisplayName("회원이 보유한 쿠폰 목록이 정상적으로 반환된다")
        fun returnsCouponItemVOList() {
            val memberId = 1L
            val couponSummary =
                StubFactory.couponSummary(id = 10L, name = "쿠폰", discountPercentage = 15L, validDays = 30)
            val coupon =
                StubFactory.coupon(
                    id = 100L,
                    couponSummary = couponSummary,
                    memberId = memberId,
                    usingAt = null,
                    now = LocalDateTime.of(2025, 12, 1, 0, 0),
                )
            every { couponOutput.findAllByMemberId(memberId) } returns listOf(coupon)

            val result = interactor.getMyCoupons(memberId)

            assertThat(result).containsExactly(
                CouponItemVO(
                    id = 100L,
                    name = "쿠폰",
                    discountPercentage = 15L,
                    expiredAt = coupon.expiredAt,
                    usingAt = null,
                ),
            )
        }

        @Test
        @DisplayName("회원이 보유한 쿠폰이 없으면 빈 리스트 반환")
        fun returnsEmptyList() {
            val memberId = 2L
            every { couponOutput.findAllByMemberId(memberId) } returns emptyList()

            val result = interactor.getMyCoupons(memberId)
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("내 쿠폰 사용")
    inner class UsingTest {
        @Test
        @DisplayName("쿠폰을 정상적으로 사용 처리한다")
        fun success() {
            // given
            val member = StubFactory.member(id = 1L, email = "user@test.com", pwd = "pwd")
            val couponSummaryId = 10L
            val couponSummary =
                StubFactory.couponSummary(id = couponSummaryId, name = "쿠폰", discountPercentage = 20L, validDays = 10)
            val coupon =
                StubFactory.coupon(
                    id = 100L,
                    couponSummary = couponSummary,
                    memberId = member.id!!,
                    usingAt = null,
                    now = LocalDateTime.of(2025, 12, 1, 0, 0),
                )
            val usedCoupon = coupon.copy(usingAt = LocalDateTime.of(2025, 9, 24, 12, 0))
            every { couponOutput.findByCouponSummaryIdAndMemberId(couponSummaryId, member.id!!) } returns Optional.of(coupon)
            every { couponOutput.save(any()) } returns usedCoupon

            // when
            val result = interactor.using(member, couponSummaryId, LocalDateTime.of(2025, 9, 24, 12, 0))

            // then
            assertThat(result.usingAt).isEqualTo(LocalDateTime.of(2025, 9, 24, 12, 0))
            verify { couponOutput.save(match { it.usingAt == LocalDateTime.of(2025, 9, 24, 12, 0) }) }
        }

        @Test
        @DisplayName("이미 사용된 쿠폰을 사용하려 하면 예외 발생")
        fun alreadyUsed_throwsException() {
            // given
            val member = StubFactory.member(id = 1L, email = "user@test.com", pwd = "pwd")
            val couponSummaryId = 10L
            val couponSummary =
                StubFactory.couponSummary(id = couponSummaryId, name = "쿠폰", discountPercentage = 20L, validDays = 10)
            val coupon =
                StubFactory.coupon(
                    id = 100L,
                    couponSummary = couponSummary,
                    memberId = member.id!!,
                    usingAt = LocalDateTime.of(2025, 9, 20, 12, 0),
                    now = LocalDateTime.of(2025, 12, 1, 0, 0),
                )
            every { couponOutput.findByCouponSummaryIdAndMemberId(couponSummaryId, member.id!!) } returns Optional.of(coupon)

            // when & then
            assertThatThrownBy {
                interactor.using(member, couponSummaryId, LocalDateTime.of(2025, 9, 24, 12, 0))
            }.isInstanceOf(kr.hhplus.be.server.domain.exception.ConflictResourceException::class.java)
                .hasMessageContaining("이미 사용된 쿠폰입니다.")
        }

        @Test
        @DisplayName("만료된 쿠폰을 사용하려 하면 예외 발생")
        fun expired_throwsException() {
            // given
            val member = StubFactory.member(id = 1L, email = "user@test.com", pwd = "pwd")
            val couponSummaryId = 10L
            val couponSummary =
                StubFactory.couponSummary(id = couponSummaryId, name = "쿠폰", discountPercentage = 20L, validDays = 10)
            val expiredAt = LocalDateTime.of(2025, 9, 20, 0, 0)
            val coupon =
                StubFactory
                    .coupon(
                        id = 100L,
                        couponSummary = couponSummary,
                        memberId = member.id!!,
                        usingAt = null,
                        now = expiredAt.minusDays(10),
                    ).copy(expiredAt = expiredAt)
            every { couponOutput.findByCouponSummaryIdAndMemberId(couponSummaryId, member.id!!) } returns Optional.of(coupon)

            // when & then
            assertThatThrownBy {
                interactor.using(member, couponSummaryId, LocalDateTime.of(2025, 9, 24, 12, 0))
            }.isInstanceOf(kr.hhplus.be.server.domain.exception.ConflictResourceException::class.java)
                .hasMessageContaining("만료된 쿠폰입니다.")
        }

        @Test
        @DisplayName("보유하지 않은 쿠폰을 사용하려 하면 예외 발생")
        fun notFound_throwsException() {
            // given
            val member = StubFactory.member(id = 2L, email = "user2@test.com", pwd = "pwd")
            val couponSummaryId = 20L
            every { couponOutput.findByCouponSummaryIdAndMemberId(couponSummaryId, member.id!!) } returns Optional.empty()

            // when & then
            assertThatThrownBy {
                interactor.using(member, couponSummaryId, LocalDateTime.of(2025, 9, 24, 12, 0))
            }.isInstanceOf(NotFoundResourceException::class.java)
                .hasMessageContaining("보유하지 않은 쿠폰입니다.")
        }
    }
}
