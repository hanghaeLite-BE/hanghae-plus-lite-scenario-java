package kr.hhplus.be.server.application.usecase.coupon

import kr.hhplus.be.server.application.vo.CouponItemVO
import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.model.member.Member
import java.time.LocalDateTime

interface MyCouponUseCase {
    fun getMyCoupons(memberId: Long): List<CouponItemVO>

    fun using(
        member: Member,
        couponSummaryId: Long,
        now: LocalDateTime = LocalDateTime.now(),
    ): Coupon
}
