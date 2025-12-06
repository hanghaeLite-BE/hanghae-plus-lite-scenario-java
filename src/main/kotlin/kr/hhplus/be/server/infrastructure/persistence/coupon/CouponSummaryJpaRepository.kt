package kr.hhplus.be.server.infrastructure.persistence.coupon

import org.springframework.data.jpa.repository.JpaRepository

interface CouponSummaryJpaRepository : JpaRepository<CouponSummaryJpaEntity, Long>
