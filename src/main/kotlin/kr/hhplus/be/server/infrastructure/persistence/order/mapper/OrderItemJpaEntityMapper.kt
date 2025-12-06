package kr.hhplus.be.server.infrastructure.persistence.order.mapper

import kr.hhplus.be.server.domain.model.order.OrderItem
import kr.hhplus.be.server.infrastructure.persistence.order.OrderItemJpaEntity

object OrderItemJpaEntityMapper {
    fun toDomain(entity: OrderItemJpaEntity): OrderItem =
        OrderItem(
            id = entity.id,
            orderSummaryId = entity.orderSummaryId,
            productSummaryId = entity.productSummaryId,
            quantity = entity.quantity,
            price = entity.price,
        )

    fun toEntity(domain: OrderItem): OrderItemJpaEntity =
        OrderItemJpaEntity(
            orderSummaryId = domain.orderSummaryId,
            productSummaryId = domain.productSummaryId,
            quantity = domain.quantity,
            price = domain.price,
        ).apply {
            id = domain.id
        }
}

