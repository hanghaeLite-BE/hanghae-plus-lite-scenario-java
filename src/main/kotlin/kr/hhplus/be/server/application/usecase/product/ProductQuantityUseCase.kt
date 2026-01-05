package kr.hhplus.be.server.application.usecase.product

import kr.hhplus.be.server.domain.model.order.OrderItem

interface ProductQuantityUseCase {
    fun reduceBy(orderItems: List<OrderItem>)
}
