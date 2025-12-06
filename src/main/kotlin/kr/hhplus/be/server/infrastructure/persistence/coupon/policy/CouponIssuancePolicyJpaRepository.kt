package kr.hhplus.be.server.infrastructure.persistence.coupon.policy

import org.springframework.data.jpa.repository.JpaRepository

interface CouponIssuancePolicyJpaRepository : JpaRepository<CouponIssuancePolicyJpaEntity, Long> {
    fun findAllByCouponIssuanceJpaEntity_Id(couponIssuanceId: Long): List<CouponIssuancePolicyJpaEntity>
}
