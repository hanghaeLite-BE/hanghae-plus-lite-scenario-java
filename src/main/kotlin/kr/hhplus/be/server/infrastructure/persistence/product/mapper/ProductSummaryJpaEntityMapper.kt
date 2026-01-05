package kr.hhplus.be.server.infrastructure.persistence.product.mapper

import kr.hhplus.be.server.domain.model.product.ProductSummary
import kr.hhplus.be.server.infrastructure.persistence.product.ProductSummaryJpaEntity

object ProductSummaryJpaEntityMapper {
    fun toDomain(entity: ProductSummaryJpaEntity): ProductSummary =
        ProductSummary(
            id = entity.id,
            name = entity.name,
            price = entity.price,
            stockQuantity = entity.stockQuantity,
        )

    fun toEntity(domain: ProductSummary): ProductSummaryJpaEntity =
        ProductSummaryJpaEntity(
            name = domain.name,
            price = domain.price,
            stockQuantity = domain.stockQuantity,
        ).apply {
            id = domain.id
        }
}