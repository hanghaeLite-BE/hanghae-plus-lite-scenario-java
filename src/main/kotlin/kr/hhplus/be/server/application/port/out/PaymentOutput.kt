package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.payment.PaymentSummary

interface PaymentOutput {
    fun save(paymentSummary: PaymentSummary): PaymentSummary
}
