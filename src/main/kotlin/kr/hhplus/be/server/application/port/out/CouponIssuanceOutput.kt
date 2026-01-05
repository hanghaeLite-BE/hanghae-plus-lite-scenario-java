package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import java.time.LocalDateTime
import java.util.Optional

interface CouponIssuanceOutput {
    fun findByCouponSummaryId(couponSummaryId: Long): Optional<CouponIssuance>

    fun atomicIssue(
        couponSummaryId: Long,
        now: LocalDateTime,
    ): Boolean
}
