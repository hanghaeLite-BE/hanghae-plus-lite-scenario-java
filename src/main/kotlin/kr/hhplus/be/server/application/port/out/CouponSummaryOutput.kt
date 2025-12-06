package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import java.util.*

interface CouponSummaryOutput {
    fun existsById(id: Long): Boolean
    fun findById(id: Long): Optional<CouponSummary>
}
