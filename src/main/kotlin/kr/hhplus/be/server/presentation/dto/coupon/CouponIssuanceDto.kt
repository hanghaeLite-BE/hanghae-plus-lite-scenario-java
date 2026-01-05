package kr.hhplus.be.server.presentation.dto.coupon

import kr.hhplus.be.server.domain.model.coupon.Coupon
import java.time.LocalDateTime

data class CouponIssuanceRequest(
    val couponSummaryId: Long,
)

data class CouponIssuanceResponse(
    val couponId: Long,
    val expiredAt: LocalDateTime?,
) {
    constructor(coupon: Coupon) : this(
        couponId = coupon.id!!,
        expiredAt = coupon.expiredAt,
    )
}
