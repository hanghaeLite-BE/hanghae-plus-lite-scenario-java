package kr.hhplus.be.server.infrastructure.persistence.coupon

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface CouponIssuanceJpaRepository : JpaRepository<CouponIssuanceJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CouponIssuanceJpaEntity c where c.couponSummaryId = :couponSummaryId")
    fun findByCouponSummaryIdWithLock(
        @Param("couponSummaryId") couponSummaryId: Long,
    ): Optional<CouponIssuanceJpaEntity>
}
