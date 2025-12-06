package kr.hhplus.be.server.application.usecase.coupon

import kr.hhplus.be.server.domain.model.coupon.Coupon
import java.time.LocalDateTime

interface CouponIssuanceUseCase {
    fun issue(
        memberId: Long,
        couponSummaryId: Long,
        now: LocalDateTime = LocalDateTime.now(),
    ): Coupon
}
