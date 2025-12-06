package kr.hhplus.be.server.application.interactor.coupon.issuance.policy

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.port.out.CouponOutput
import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@UnitTest
class OnePerMemberPolicyInteractorTest {
    private val couponOutput: CouponOutput = mockk()
    private val interactor = OnePerMemberPolicyInteractor(couponOutput)

    @Test
    @DisplayName("이미 해당 쿠폰을 보유한 회원은 예외 발생")
    fun alreadyIssuedCoupon_throwsException() {
        val memberId = 1L
        val couponSummary = CouponSummary(1L, "쿠폰", 10L, 10)
        val issuedCoupon = Coupon(10L, memberId, couponSummary, null, null)
        every { couponOutput.findAllByMemberId(memberId) } returns listOf(issuedCoupon)

        assertThatThrownBy {
            interactor.canIssue(memberId, couponSummary)
        }.isInstanceOf(ConflictResourceException::class.java)
            .hasMessageContaining("중복발급이 제한된 쿠폰입니다.")
    }

    @Test
    @DisplayName("해당 쿠폰을 보유하지 않은 회원은 예외 없이 통과")
    fun notIssuedCoupon_passes() {
        // given
        val memberId = 2L
        val couponSummary = CouponSummary(2L, "쿠폰", 10L, 10)
        every { couponOutput.findAllByMemberId(memberId) } returns emptyList()

        // when & then
        assertDoesNotThrow {
            interactor.canIssue(memberId, couponSummary)
        }
    }
}
