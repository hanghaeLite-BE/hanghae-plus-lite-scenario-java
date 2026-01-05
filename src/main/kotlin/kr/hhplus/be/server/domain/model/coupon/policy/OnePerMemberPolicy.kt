package kr.hhplus.be.server.domain.model.coupon.policy

import kr.hhplus.be.server.domain.model.coupon.CouponIssuance

/**
 * 회원당 1회 발급 정책(중복발급X)
 */
data class OnePerMemberPolicy(
    override val id: Long?,
    override val couponIssuance: CouponIssuance,
) : CouponIssuancePolicy(id, couponIssuance) {
    fun getCouponSummaryId(): Long = couponIssuance.couponSummaryId
}