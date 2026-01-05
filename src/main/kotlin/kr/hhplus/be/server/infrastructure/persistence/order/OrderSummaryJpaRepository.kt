package kr.hhplus.be.server.infrastructure.persistence.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface OrderSummaryJpaRepository : JpaRepository<OrderSummaryJpaEntity, Long> {
    @Query(
        """
        SELECT o FROM OrderSummaryJpaEntity o
        LEFT JOIN FETCH o.orderItems oi
        WHERE o.id = :id
    """,
    )
    fun findByIdWithOrderItems(id: Long): Optional<OrderSummaryJpaEntity>
}
