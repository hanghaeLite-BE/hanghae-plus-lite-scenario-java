package kr.hhplus.be.server.infrastructure.persistence.order.mapper

import kr.hhplus.be.server.domain.model.order.OrderItem
import kr.hhplus.be.server.domain.model.order.OrderSummary
import kr.hhplus.be.server.infrastructure.persistence.order.OrderItemJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.order.OrderSummaryJpaEntity

object OrderSummaryJpaEntityMapper {
    fun toDomain(
        entity: OrderSummaryJpaEntity,
        orderItemEntityTo: (entity: OrderItemJpaEntity) -> OrderItem
    ): OrderSummary =
        OrderSummary(
            id = entity.id,
            memberId = entity.memberId,
            orderItems = entity.orderItems.map(orderItemEntityTo),
        )

    fun toEntity(
        domain: OrderSummary,
        orderItemDomainToEntity: (domain: OrderItem) -> OrderItemJpaEntity
    ): OrderSummaryJpaEntity {
        val orderItemEntities = domain.orderItems.map {
            orderItemDomainToEntity(it)
        }

        val orderSummaryJpaEntity = OrderSummaryJpaEntity(domain.memberId)
            .apply { id = domain.id }
            .addOrderItems(orderItemEntities)

        return orderSummaryJpaEntity
    }
}

