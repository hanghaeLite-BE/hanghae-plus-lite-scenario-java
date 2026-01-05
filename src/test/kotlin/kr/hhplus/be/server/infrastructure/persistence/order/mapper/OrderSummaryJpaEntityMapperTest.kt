package kr.hhplus.be.server.infrastructure.persistence.order.mapper

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kr.hhplus.be.server.domain.model.order.OrderItem
import kr.hhplus.be.server.domain.model.order.OrderSummary
import kr.hhplus.be.server.infrastructure.persistence.order.OrderItemJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.order.OrderSummaryJpaEntity
import org.junit.jupiter.api.DisplayName

class OrderSummaryJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 OrderSummaryJpaEntity를 OrderSummary로 변환한다")
    fun toDomainOrderSummaryEntity() {
        val orderItemEntity = mockk<OrderItemJpaEntity>()
        val orderItem = mockk<OrderItem>()
        val entity = OrderSummaryJpaEntity(
            memberId = 1L
        ).apply {
            id = 10L
            addOrderItems(listOf(orderItemEntity))
        }
        val orderItemEntityTo: (OrderItemJpaEntity) -> OrderItem = { orderItem }

        val domain = OrderSummaryJpaEntityMapper.toDomain(entity, orderItemEntityTo)

        assertEquals(entity.id, domain.id)
        assertEquals(entity.memberId, domain.memberId)
        assertEquals(listOf(orderItem), domain.orderItems)
    }

    @Test
    @DisplayName("toEntity는 OrderSummary를 OrderSummaryJpaEntity로 변환한다")
    fun toEntityOrderSummary() {
        val orderItem = mockk<OrderItem>()
        val orderItemEntity = mockk<OrderItemJpaEntity>()
        val domain = OrderSummary(
            id = 20L,
            memberId = 2L,
            orderItems = listOf(orderItem)
        )
        val orderItemDomainToEntity: (OrderItem) -> OrderItemJpaEntity = { orderItemEntity }

        val entity = OrderSummaryJpaEntityMapper.toEntity(domain, orderItemDomainToEntity)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.memberId, entity.memberId)
        assertEquals(listOf(orderItemEntity), entity.orderItems)
    }
}
