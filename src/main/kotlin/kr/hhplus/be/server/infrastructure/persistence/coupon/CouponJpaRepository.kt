package kr.hhplus.be.server.infrastructure.persistence.coupon

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.Optional

interface CouponJpaRepository : JpaRepository<CouponJpaEntity, Long> {
    @Query(
        """
            select c
            from CouponJpaEntity c
            where c.couponSummaryJpaEntity.id = :couponSummaryId
            and c.memberId = :memberId
            """,
    )
    fun findByCouponSummaryIdAndMemberId(
        @Param("couponSummaryId") couponSummaryId: Long,
        @Param("memberId") memberId: Long,
    ): Optional<CouponJpaEntity>

    fun findAllByMemberId(memberId: Long): List<CouponJpaEntity>

    @EntityGraph(attributePaths = ["couponSummaryJpaEntity"])
    @Query(
        """
            select c
            from CouponJpaEntity c
            where c.memberId = :memberId
        """,
    )
    fun findAllWithSummaryByMemberId(
        @Param("memberId") memberId: Long,
    ): List<CouponJpaEntity>

    @Modifying
    @Query(
        """
        UPDATE CouponJpaEntity c
        SET c.usingAt = :now
        WHERE c.couponSummaryJpaEntity.id = :couponSummaryId
          AND c.memberId = :memberId
          AND c.usingAt IS NULL
          AND (c.expiredAt IS NULL OR c.expiredAt > :now)
        """,
    )
    fun atomicUse(
        @Param("couponSummaryId") couponSummaryId: Long,
        @Param("memberId") memberId: Long,
        @Param("now") now: LocalDateTime,
    ): Int
}
