package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.OrderSummaryOutput
import kr.hhplus.be.server.domain.model.order.OrderSummary
import kr.hhplus.be.server.infrastructure.persistence.order.OrderSummaryJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.order.mapper.OrderItemJpaEntityMapper
import kr.hhplus.be.server.infrastructure.persistence.order.mapper.OrderSummaryJpaEntityMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Component
class OrderSummaryAdapter(
    private val repository: OrderSummaryJpaRepository,
) : OrderSummaryOutput {
    @Transactional
    override fun save(domain: OrderSummary): OrderSummary {
        val entity =
            OrderSummaryJpaEntityMapper
                .toEntity(
                    domain = domain,
                    orderItemDomainToEntity = OrderItemJpaEntityMapper::toEntity,
                ).let(repository::save)

        return OrderSummaryJpaEntityMapper.toDomain(
            entity = entity,
            orderItemEntityTo = OrderItemJpaEntityMapper::toDomain,
        )
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): Optional<OrderSummary> =
        repository.findById(id).map {
            OrderSummaryJpaEntityMapper.toDomain(
                entity = it,
                orderItemEntityTo = OrderItemJpaEntityMapper::toDomain,
            )
        }

    @Transactional(readOnly = true)
    override fun findByIdWithOrderItems(id: Long): Optional<OrderSummary> =
        repository.findByIdWithOrderItems(id).map {
            OrderSummaryJpaEntityMapper.toDomain(
                entity = it,
                orderItemEntityTo = OrderItemJpaEntityMapper::toDomain,
            )
        }
}
