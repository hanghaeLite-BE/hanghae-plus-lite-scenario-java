package kr.hhplus.be.server.infrastructure.persistence.order.mapper

import kr.hhplus.be.server.domain.model.order.OrderItem
import kr.hhplus.be.server.infrastructure.persistence.order.OrderItemJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OrderItemJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 OrderItemJpaEntity를 OrderItem으로 변환한다")
    fun toDomain() {
        val entity = OrderItemJpaEntity(
            orderSummaryId = 1L,
            productSummaryId = 2L,
            quantity = 3,
            price = 1000
        ).apply { id = 10L }

        val domain = OrderItemJpaEntityMapper.toDomain(entity)

        assertEquals(entity.id, domain.id)
        assertEquals(entity.orderSummaryId, domain.orderSummaryId)
        assertEquals(entity.productSummaryId, domain.productSummaryId)
        assertEquals(entity.quantity, domain.quantity)
        assertEquals(entity.price, domain.price)
    }

    @Test
    @DisplayName("toEntity는 OrderItem을 OrderItemJpaEntity로 변환한다")
    fun toEntity() {
        val domain = OrderItem(
            id = 20L,
            orderSummaryId = 2L,
            productSummaryId = 3L,
            quantity = 4,
            price = 2000
        )

        val entity = OrderItemJpaEntityMapper.toEntity(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.orderSummaryId, entity.orderSummaryId)
        assertEquals(domain.productSummaryId, entity.productSummaryId)
        assertEquals(domain.quantity, entity.quantity)
        assertEquals(domain.price, entity.price)
    }
}