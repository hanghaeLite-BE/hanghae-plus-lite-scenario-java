package kr.hhplus.be.server.application.usecase.payment

import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.model.order.OrderSummary
import kr.hhplus.be.server.domain.model.payment.PaymentSummary
import org.springframework.stereotype.Service

@Service
interface GeneratePaymentUseCase {
    /**
     * 결제정보 생성 usecase
     */
    fun generatePaymentSummary(
        coupon: Coupon?,
        orderSummary: OrderSummary,
        method: String,
    ): PaymentSummary
}
