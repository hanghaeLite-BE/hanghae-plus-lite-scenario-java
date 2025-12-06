package kr.hhplus.be.server.infrastructure.persistence.coupon

import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
    fun findAllWithSummaryByMemberId(@Param("memberId") memberId: Long): List<CouponJpaEntity>
}
