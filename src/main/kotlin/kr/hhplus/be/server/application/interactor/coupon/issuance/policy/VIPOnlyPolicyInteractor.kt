package kr.hhplus.be.server.application.interactor.coupon.issuance.policy

import kr.hhplus.be.server.application.port.out.MemberOutput
import kr.hhplus.be.server.application.usecase.coupon.CouponIssuancePolicyUseCase
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import kr.hhplus.be.server.domain.exception.ForbiddenException
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.domain.model.coupon.policy.VIPOnlyPolicy
import org.springframework.stereotype.Service

/**
 * VIP 회원에게만 발급 가능한 정책
 */
@Service
class VIPOnlyPolicyInteractor(
    private val memberOutput: MemberOutput,
) : CouponIssuancePolicyUseCase<VIPOnlyPolicy> {
    override fun genericClassInfo(): Class<VIPOnlyPolicy> {
        return VIPOnlyPolicy::class.java
    }

    override fun canIssue(memberId: Long, couponSummary: CouponSummary) {
        val member = memberOutput.findById(memberId).orElseThrow {
            NotFoundResourceException(
                message = "존재하지 않는 회원입니다.",
                clue = mapOf("memberId" to memberId),
            )
        }

        if (!member.isVIP()) {
            throw ForbiddenException(
                message = "VIP회원만 발급 가능한 쿠폰입니다.",
                clue = mapOf("memberId" to memberId, "memberType" to member.memberType),
            )
        }
    }
}
