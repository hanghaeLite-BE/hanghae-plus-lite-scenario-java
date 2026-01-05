package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.order.OrderItem

interface OrderItemOutput {
    fun saveAll(orderItems: Collection<OrderItem>): Set<OrderItem>
}
