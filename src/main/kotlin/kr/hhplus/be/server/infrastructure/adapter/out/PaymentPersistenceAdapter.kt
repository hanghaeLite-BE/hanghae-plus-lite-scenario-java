package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.PaymentOutput
import kr.hhplus.be.server.domain.model.payment.PaymentSummary
import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentSummaryJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.payment.mapper.PaymentSummaryJpaEntityMapper
import org.springframework.stereotype.Component

@Component
class PaymentPersistenceAdapter(
    private val paymentSummaryJpaRepository: PaymentSummaryJpaRepository,
) : PaymentOutput {
    override fun save(paymentSummary: PaymentSummary): PaymentSummary {
        val entity = PaymentSummaryJpaEntityMapper.toEntity(paymentSummary)
            .let(paymentSummaryJpaRepository::save)

        return PaymentSummaryJpaEntityMapper.toDomain(entity)
    }
}
