package kr.hhplus.be.server.infrastructure.persistence.payment

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentSummaryJpaRepository : JpaRepository<PaymentSummaryJpaEntity, Long>
