package kr.hhplus.be.server.application.interactor.order

import kr.hhplus.be.server.application.port.out.PlaceOrderOutput
import kr.hhplus.be.server.application.usecase.order.GenerateOrderUseCase
import kr.hhplus.be.server.application.vo.PlaceOrderItemVO
import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.domain.model.order.OrderItem
import kr.hhplus.be.server.domain.model.order.OrderSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceOrderInteractor(
    private val placeOrderOutput: PlaceOrderOutput,
) : GenerateOrderUseCase {
    /**
     * 주문생성
     * 1. 주문 상품개별정보 생성
     * 2. 전체 주문 생성
     */
    @Transactional
    override fun generateOrder(
        member: Member,
        orderItems: List<PlaceOrderItemVO>,
    ): OrderSummary {
        val orderSummary =
            OrderSummary.placeOrder(
                memberId = member.id!!,
                orderItems = emptyList(),
            ).let(placeOrderOutput::saveOrder)

        val orderItemList =
            orderItems.map {
                OrderItem(
                    id = null,
                    orderSummaryId = orderSummary.id!!,
                    productSummaryId = it.productSummaryId,
                    quantity = it.quantity,
                    price = it.price,
                )
            }.let(placeOrderOutput::addOrderItems)

        return orderSummary.addOrderItems(
            orderItems = orderItemList,
        )
    }
}
