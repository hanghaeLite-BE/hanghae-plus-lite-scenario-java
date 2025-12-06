package kr.hhplus.be.server.application.interactor.coupon.issuance.policy

import kr.hhplus.be.server.application.port.out.CouponOutput
import kr.hhplus.be.server.application.usecase.coupon.CouponIssuancePolicyUseCase
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.domain.model.coupon.policy.OnePerMemberPolicy
import org.springframework.stereotype.Service

@Service
class OnePerMemberPolicyInteractor(
    private val couponOutput: CouponOutput,
) : CouponIssuancePolicyUseCase<OnePerMemberPolicy> {
    override fun genericClassInfo(): Class<OnePerMemberPolicy> {
        return OnePerMemberPolicy::class.java
    }

    override fun canIssue(
        memberId: Long,
        couponSummary: CouponSummary,
    ) {
        val couponSummaryId = couponSummary.id
        val alreadyIssuedCoupon =
            couponOutput.findAllByMemberId(memberId = memberId)
                .find { it.couponSummary.id == couponSummaryId }

        if (alreadyIssuedCoupon != null) {
            throw ConflictResourceException(
                message = "중복발급이 제한된 쿠폰입니다.",
                clue = mapOf(
                    "memberId" to "$memberId",
                    "couponSummaryId" to "$couponSummaryId",
                    "alreadyIssuedCouponId" to "${alreadyIssuedCoupon.id}",
                ),
            )
        }
    }
}
