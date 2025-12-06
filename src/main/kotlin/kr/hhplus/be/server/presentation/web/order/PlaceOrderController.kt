package kr.hhplus.be.server.presentation.web.order

import kr.hhplus.be.server.application.usecase.order.PlaceOrderUseCase
import kr.hhplus.be.server.presentation.dto.order.PlaceOrderRequest
import kr.hhplus.be.server.presentation.dto.order.PlaceOrderResponse
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentContext
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentRequestContext
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentRequestResponseRecorder
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class PlaceOrderController(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val idempotentRequestResponseRecorder: IdempotentRequestResponseRecorder,
) {
    @PostMapping
    fun placeOrder(
        @RequestHeader("X-Member-Id") memberId: Long,
        @RequestBody requestData: PlaceOrderRequest,
        @IdempotentContext context: IdempotentRequestContext<PlaceOrderResponse>,
    ): PlaceOrderResponse =
        context.getResponseOrElse {
            val placeOrderResult =
                placeOrderUseCase.placeOrder(
                    memberId = memberId,
                    couponSummaryId = requestData.couponSummaryId,
                    orderItems = requestData.orderItems.map { it.toVO() },
                    requestPaymentSummary = requestData.paymentSummary.toVO(),
                )

            val response = PlaceOrderResponse(placeOrderResult)

            idempotentRequestResponseRecorder.recordSuccess(
                cacheKey = context.cacheKey,
                responseBody = response,
                statusCode = HttpStatus.OK.value(),
                responseType = PlaceOrderResponse::class.java,
            )
        }
}
