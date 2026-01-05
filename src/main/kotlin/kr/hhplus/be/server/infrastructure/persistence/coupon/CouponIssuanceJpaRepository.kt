package kr.hhplus.be.server.infrastructure.persistence.coupon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.Optional

interface CouponIssuanceJpaRepository : JpaRepository<CouponIssuanceJpaEntity, Long> {
    fun findByCouponSummaryId(couponSummaryId: Long): Optional<CouponIssuanceJpaEntity>

    @Modifying
    @Query(
        """
        UPDATE CouponIssuanceJpaEntity c
        SET c.issuedCount = c.issuedCount + 1
        WHERE c.couponSummaryId = :couponSummaryId
          AND (c.maxCount IS NULL OR c.issuedCount < c.maxCount)
          AND c.startAt <= :now
          AND c.endAt >= :now
        """,
    )
    fun atomicIssue(
        @Param("couponSummaryId") couponSummaryId: Long,
        @Param("now") now: LocalDateTime,
    ): Int
}
