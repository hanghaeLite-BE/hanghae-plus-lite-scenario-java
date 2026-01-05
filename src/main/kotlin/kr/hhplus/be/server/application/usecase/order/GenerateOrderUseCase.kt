package kr.hhplus.be.server.application.usecase.order

import kr.hhplus.be.server.application.vo.PlaceOrderItemVO
import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.domain.model.order.OrderSummary

interface GenerateOrderUseCase {
    fun generateOrder(
        member: Member,
        orderItems: List<PlaceOrderItemVO>,
    ): OrderSummary
}
