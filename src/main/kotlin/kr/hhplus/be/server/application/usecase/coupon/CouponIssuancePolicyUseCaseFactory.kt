package kr.hhplus.be.server.application.usecase.coupon

import kr.hhplus.be.server.domain.model.coupon.policy.CouponIssuancePolicy
import org.springframework.stereotype.Component

@Component
class CouponIssuancePolicyUseCaseFactory(
    private val policyUseCaseList: List<CouponIssuancePolicyUseCase<*>>,
) {
    fun from(couponIssuancePolicy: CouponIssuancePolicy): CouponIssuancePolicyUseCase<*>? =
        policyUseCaseList.find {
            it.genericClassInfo()
                .isAssignableFrom(couponIssuancePolicy.javaClass)
        }
}
