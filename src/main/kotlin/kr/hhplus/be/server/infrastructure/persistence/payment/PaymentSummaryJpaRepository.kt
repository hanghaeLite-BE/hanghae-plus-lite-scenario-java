package kr.hhplus.be.server.infrastructure.persistence.payment

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentSummaryJpaRepository : JpaRepository<PaymentSummaryJpaEntity, Long> {
    fun findByOrderSummaryId(orderId: Long): Optional<PaymentSummaryJpaEntity>
}
