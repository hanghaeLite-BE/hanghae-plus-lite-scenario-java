package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.PlaceOrderOutput
import kr.hhplus.be.server.domain.model.order.OrderItem
import kr.hhplus.be.server.domain.model.order.OrderSummary
import kr.hhplus.be.server.infrastructure.persistence.order.OrderItemJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.order.OrderSummaryJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.order.mapper.OrderItemJpaEntityMapper
import kr.hhplus.be.server.infrastructure.persistence.order.mapper.OrderSummaryJpaEntityMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PlaceOrderPersistenceAdapter(
    private val orderSummaryJpaRepository: OrderSummaryJpaRepository,
    private val orderItemJpaRepository: OrderItemJpaRepository,
) : PlaceOrderOutput {
    @Transactional
    override fun saveOrder(orderSummary: OrderSummary): OrderSummary {
        val orderSummaryJpaEntity = OrderSummaryJpaEntityMapper.toEntity(
            domain = orderSummary,
            orderItemDomainToEntity = OrderItemJpaEntityMapper::toEntity,
        ).let(orderSummaryJpaRepository::save)

        return OrderSummaryJpaEntityMapper.toDomain(
            entity = orderSummaryJpaEntity,
            orderItemEntityTo = OrderItemJpaEntityMapper::toDomain,
        )
    }

    @Transactional
    override fun addOrderItems(orderItemList: List<OrderItem>): List<OrderItem> {
        val orderItemJpaEntities = orderItemList.map {
            OrderItemJpaEntityMapper.toEntity(it)
        }.let(orderItemJpaRepository::saveAll)

        return orderItemJpaEntities
            .map(OrderItemJpaEntityMapper::toDomain)
    }
}
