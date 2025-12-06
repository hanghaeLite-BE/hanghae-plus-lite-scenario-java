package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.coupon.Coupon
import java.util.*

interface CouponOutput {
    fun findByIdAndMemberId(
        couponSummaryId: Long,
        memberId: Long,
    ): Optional<Coupon>

    fun findAllByMemberId(memberId: Long): List<Coupon>

    fun save(coupon: Coupon): Coupon
}
