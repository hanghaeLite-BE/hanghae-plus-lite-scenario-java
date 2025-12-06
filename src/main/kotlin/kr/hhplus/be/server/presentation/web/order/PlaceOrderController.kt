package kr.hhplus.be.server.presentation.web.order

import kr.hhplus.be.server.application.usecase.order.PlaceOrderUseCase
import kr.hhplus.be.server.presentation.dto.order.PlaceOrderRequest
import kr.hhplus.be.server.presentation.dto.order.PlaceOrderResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class PlaceOrderController(
    private val placeOrderUseCase: PlaceOrderUseCase,
) {
    @PostMapping
    fun placeOrder(
        @RequestHeader("X-Member-Id") memberId: Long,
        @RequestBody requestData: PlaceOrderRequest,
    ): PlaceOrderResponse {
        val placeOrderResult =
            placeOrderUseCase.placeOrder(
                memberId = memberId,
                couponSummaryId = requestData.couponSummaryId,
                orderItems = requestData.orderItems.map { it.toVO() },
                requestPaymentSummary = requestData.paymentSummary.toVO(),
            )

        return PlaceOrderResponse(placeOrderResult)
    }
}
