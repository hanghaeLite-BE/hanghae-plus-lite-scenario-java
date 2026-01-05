package kr.hhplus.be.server.domain.model.coupon.policy

import kr.hhplus.be.server.domain.model.coupon.CouponIssuance

/**
 * 발급 정책 인터페이스
 */
sealed class CouponIssuancePolicy(
    open val id: Long?,
    open val couponIssuance: CouponIssuance,
)
