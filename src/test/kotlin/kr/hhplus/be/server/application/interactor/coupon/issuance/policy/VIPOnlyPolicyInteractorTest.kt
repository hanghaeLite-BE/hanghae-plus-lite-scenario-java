package kr.hhplus.be.server.application.interactor.coupon.issuance.policy

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.port.out.MemberOutput
import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.exception.ForbiddenException
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.domain.model.member.MemberType
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*

@UnitTest
class VIPOnlyPolicyInteractorTest {
    private val memberOutput: MemberOutput = mockk()
    private val interactor = VIPOnlyPolicyInteractor(memberOutput)

    @Test
    @DisplayName("VIP 회원은 예외 없이 통과")
    fun vipMember_passes() {
        // given
        val memberId = 1L
        val couponSummary = CouponSummary(1L, "쿠폰", 10L, 10)
        val vipMember = Member(memberId, "vip@test.com", "pwd", MemberType.VIP)
        every { memberOutput.findById(memberId) } returns Optional.of(vipMember)

        // when & then
        assertDoesNotThrow {
            interactor.canIssue(memberId, couponSummary)
        }
    }

    @Test
    @DisplayName("일반 회원은 예외 발생")
    fun generalMember_throwsException() {
        // given
        val memberId = 2L
        val couponSummary = CouponSummary(2L, "쿠폰", 10L, 10)
        val generalMember = Member(memberId, "user@test.com", "pwd", MemberType.GENERAL)
        every { memberOutput.findById(memberId) } returns Optional.of(generalMember)

        // when & then
        assertThatThrownBy {
            interactor.canIssue(memberId, couponSummary)
        }.isInstanceOf(ForbiddenException::class.java)
            .hasMessageContaining("VIP회원만 발급 가능한 쿠폰입니다.")
    }

    @Test
    @DisplayName("존재하지 않는 회원은 예외 발생")
    fun notFoundMember_throwsException() {
        // given
        val memberId = 999L
        val couponSummary = CouponSummary(3L, "쿠폰", 10L, 10)
        every { memberOutput.findById(memberId) } returns Optional.empty()

        // when & then
        assertThatThrownBy {
            interactor.canIssue(memberId, couponSummary)
        }.isInstanceOf(NotFoundResourceException::class.java)
            .hasMessageContaining("존재하지 않는 회원입니다.")
    }
}
