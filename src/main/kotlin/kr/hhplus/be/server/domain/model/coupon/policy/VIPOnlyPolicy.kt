package kr.hhplus.be.server.domain.model.coupon.policy

import kr.hhplus.be.server.domain.model.coupon.CouponIssuance

/**
 * VIP 회원만 발급 정책
 */
data class VIPOnlyPolicy(
    override val id: Long?,
    override val couponIssuance: CouponIssuance,
) : CouponIssuancePolicy(id, couponIssuance)
