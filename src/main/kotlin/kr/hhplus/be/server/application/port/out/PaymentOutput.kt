package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.payment.PaymentSummary
import java.util.Optional

interface PaymentOutput {
    fun save(paymentSummary: PaymentSummary): PaymentSummary

    fun findByOrderSummaryId(orderId: Long): Optional<PaymentSummary>
}
