package kr.hhplus.be.server.presentation.dto.coupon

import kr.hhplus.be.server.application.vo.CouponItemVO
import java.time.LocalDateTime

data class MyCouponsResponse(
    val count: Int = coupons.size,
    val coupons: List<CouponItem>,
) {
    constructor(coupons: List<CouponItemVO>) : this(
        coupons =
            coupons.map {
                CouponItem(
                    id = it.id,
                    name = it.name,
                    discountPercentage = it.discountPercentage,
                    expiredAt = it.expiredAt,
                    usingAt = it.usingAt,
                )
            },
    )
}

data class CouponItem(
    val id: Long,
    val name: String,
    val discountPercentage: Long,
    val expiredAt: LocalDateTime?,
    val usingAt: LocalDateTime?,
)
