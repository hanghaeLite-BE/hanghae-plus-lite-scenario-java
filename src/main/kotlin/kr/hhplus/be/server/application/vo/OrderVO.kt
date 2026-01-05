package kr.hhplus.be.server.application.vo

import kr.hhplus.be.server.domain.model.order.OrderSummary
import kr.hhplus.be.server.domain.model.payment.PaymentSummary

data class PlaceOrderResultVO(
    val orderId: Long,
    val paymentMethod: String,
    val paymentChargeAmount: Long,
    val paymentDiscountAmount: Long,
    val paymentTotalAmount: Long,
    val orderItems: List<PlaceOrderItemVO>,
) {
    companion object {
        fun of(
            orderSummary: OrderSummary,
            paymentSummary: PaymentSummary,
        ): PlaceOrderResultVO =
            PlaceOrderResultVO(
                orderId = orderSummary.id!!,
                paymentMethod = paymentSummary.method.name,
                paymentTotalAmount = paymentSummary.totalAmount,
                paymentDiscountAmount = paymentSummary.discountAmount,
                paymentChargeAmount = paymentSummary.chargeAmount,
                orderItems =
                    orderSummary.orderItems.map {
                        PlaceOrderItemVO(
                            productSummaryId = it.productSummaryId,
                            price = it.price,
                            quantity = it.quantity,
                        )
                    },
            )
    }
}

data class PlaceOrderItemVO(
    val quantity: Int,
    val price: Int,
    val productSummaryId: Long,
)

data class PlaceOrderPaymentSummaryVO(
    val method: String,
    val totalAmount: Long,
    val discountAmount: Long,
    val chargeAmount: Long,
)
