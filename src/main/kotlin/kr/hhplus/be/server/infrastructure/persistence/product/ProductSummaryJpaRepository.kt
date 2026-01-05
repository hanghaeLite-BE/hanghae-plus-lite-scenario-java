package kr.hhplus.be.server.infrastructure.persistence.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface ProductSummaryJpaRepository : JpaRepository<ProductSummaryJpaEntity, Long> {
    fun findAllByIdIn(ids: Collection<Long>): List<ProductSummaryJpaEntity>

    @Modifying
    @Query(
        """
        UPDATE ProductSummaryJpaEntity p
        SET p.stockQuantity = p.stockQuantity - :quantity
        WHERE p.id = :id AND p.stockQuantity >= :quantity
        """,
    )
    fun reduceStock(
        id: Long,
        quantity: Int,
    ): Int

    @Query(
        value =
            """
            SELECT
                ps.id,
                ps.name,
                ps.price,
                ps.stock_quantity,
                COUNT(oi.id) AS total_order_count
            FROM
                order_item oi
            JOIN
                product_summary ps ON oi.product_summary_id = ps.id
            WHERE
                oi.created_at >= :startAt
            GROUP BY
                ps.id, ps.name, ps.price, ps.stock_quantity, ps.created_at, ps.updated_at
            ORDER BY
                total_order_count DESC
            LIMIT :limit
            """,
        nativeQuery = true,
    )
    fun topSellingProductsInNDays(
        startAt: LocalDateTime,
        limit: Int,
    ): List<TopSellingProductSummary>
}

interface TopSellingProductSummary {
    fun getId(): Long

    fun getName(): String

    fun getPrice(): Int

    fun getStockQuantity(): Int

    fun getTotalOrderCount(): Int
}
