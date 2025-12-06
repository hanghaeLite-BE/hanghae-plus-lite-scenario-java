package kr.hhplus.be.server.infrastructure.persistence.coupon

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CouponIssuanceJpaRepository : JpaRepository<CouponIssuanceJpaEntity, Long> {
    fun findByCouponSummaryId(couponSummaryId: Long): Optional<CouponIssuanceJpaEntity>
}
