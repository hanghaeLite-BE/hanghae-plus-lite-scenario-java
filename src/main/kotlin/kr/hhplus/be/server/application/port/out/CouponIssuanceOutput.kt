package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import java.util.Optional

interface CouponIssuanceOutput {
    fun findByCouponSummaryIdWithLock(couponSummaryId: Long): Optional<CouponIssuance>

    fun save(domain: CouponIssuance): CouponIssuance
}
