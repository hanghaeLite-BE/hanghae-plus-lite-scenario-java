package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.coupon.Coupon
import java.time.LocalDateTime
import java.util.Optional

interface CouponOutput {
    fun findByCouponSummaryIdAndMemberId(
        couponSummaryId: Long,
        memberId: Long,
    ): Optional<Coupon>

    fun findAllByMemberId(memberId: Long): List<Coupon>

    fun save(coupon: Coupon): Coupon

    fun atomicUse(
        couponSummaryId: Long,
        memberId: Long,
        now: LocalDateTime,
    ): Boolean

    fun refresh(coupon: Coupon): Coupon
}
