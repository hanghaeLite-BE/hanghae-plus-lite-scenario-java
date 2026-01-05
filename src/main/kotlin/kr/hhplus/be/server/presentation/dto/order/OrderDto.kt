package kr.hhplus.be.server.presentation.dto.order

import kr.hhplus.be.server.application.vo.PlaceOrderItemVO
import kr.hhplus.be.server.application.vo.PlaceOrderPaymentSummaryVO
import kr.hhplus.be.server.application.vo.PlaceOrderResultVO

data class PlaceOrderRequest(
    val couponSummaryId: Long?,
    val orderItems: List<OrderItemDto>,
    val paymentSummary: PaymentSummaryDto,
)

data class OrderItemDto(
    val productSummaryId: Long,
    val price: Int,
    val quantity: Int,
) {
    fun toVO(): PlaceOrderItemVO =
        PlaceOrderItemVO(
            productSummaryId = productSummaryId,
            price = price,
            quantity = quantity,
        )
}

data class PaymentSummaryDto(
    val method: String,
    val totalAmount: Long,
    val discountAmount: Long,
    val chargeAmount: Long,
) {
    fun toVO(): PlaceOrderPaymentSummaryVO =
        PlaceOrderPaymentSummaryVO(
            method = method,
            totalAmount = totalAmount,
            discountAmount = discountAmount,
            chargeAmount = chargeAmount,
        )
}

data class PlaceOrderResponse(
    val orderId: Long,
    val paymentSummary: PaymentSummaryDto,
    val orderItems: List<OrderItemDto>,
) {
    constructor(placeOrderResult: PlaceOrderResultVO) : this(
        orderId = placeOrderResult.orderId,
        paymentSummary =
            PaymentSummaryDto(
                method = placeOrderResult.paymentMethod,
                totalAmount = placeOrderResult.paymentTotalAmount,
                discountAmount = placeOrderResult.paymentDiscountAmount,
                chargeAmount = placeOrderResult.paymentChargeAmount,
            ),
        orderItems =
            placeOrderResult.orderItems.map {
                OrderItemDto(
                    productSummaryId = it.productSummaryId,
                    price = it.price,
                    quantity = it.quantity,
                )
            },
    )
}
