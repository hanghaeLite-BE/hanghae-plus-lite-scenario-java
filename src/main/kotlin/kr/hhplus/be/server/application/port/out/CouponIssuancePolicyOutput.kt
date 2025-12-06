package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.coupon.policy.CouponIssuancePolicy

interface CouponIssuancePolicyOutput {
    fun findAllByCouponIssuanceId(couponIssuanceId: Long): List<CouponIssuancePolicy>
}
