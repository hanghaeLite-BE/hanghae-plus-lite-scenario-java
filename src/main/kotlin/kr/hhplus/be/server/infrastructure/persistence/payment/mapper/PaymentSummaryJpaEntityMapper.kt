package kr.hhplus.be.server.infrastructure.persistence.payment.mapper

import kr.hhplus.be.server.domain.model.payment.PaymentMethod
import kr.hhplus.be.server.domain.model.payment.PaymentSummary
import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentSummaryJpaEntity

object PaymentSummaryJpaEntityMapper {
    fun toDomain(entity: PaymentSummaryJpaEntity): PaymentSummary =
        PaymentSummary(
            id = entity.id,
            method = PaymentMethod.from(entity.method.name)!!,
            totalAmount = entity.totalAmount,
            discountAmount = entity.discountAmount,
            chargeAmount = entity.chargeAmount,
            memberId = entity.memberId,
            orderSummaryId = entity.orderSummaryId,
            couponId = entity.couponId,
        )

    fun toEntity(domain: PaymentSummary): PaymentSummaryJpaEntity =
        PaymentSummaryJpaEntity(
            method = kr.hhplus.be.server.infrastructure.persistence.payment.PaymentMethod.valueOf(domain.method.name),
            totalAmount = domain.totalAmount,
            discountAmount = domain.discountAmount,
            chargeAmount = domain.chargeAmount,
            memberId = domain.memberId,
            orderSummaryId = domain.orderSummaryId,
            couponId = domain.couponId,
        ).apply {
            id = domain.id
        }
}

