package kr.hhplus.be.server.infrastructure.persistence.payment.mapper

import kr.hhplus.be.server.domain.model.payment.PaymentMethod
import kr.hhplus.be.server.domain.model.payment.PaymentSummary
import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentSummaryJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PaymentSummaryJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 PaymentSummaryJpaEntity를 PaymentSummary로 변환한다")
    fun toDomain() {
        val entity =
            PaymentSummaryJpaEntity(
                method = kr.hhplus.be.server.infrastructure.persistence.payment.PaymentMethod.POINT,
                totalAmount = 10000,
                discountAmount = 1000,
                chargeAmount = 9000,
                orderSummaryId = 2L,
                couponId = 3L,
            ).apply { id = 10L }

        val domain = PaymentSummaryJpaEntityMapper.toDomain(entity)

        assertEquals(entity.id, domain.id)
        assertEquals(PaymentMethod.POINT, domain.method)
        assertEquals(entity.totalAmount, domain.totalAmount)
        assertEquals(entity.discountAmount, domain.discountAmount)
        assertEquals(entity.chargeAmount, domain.chargeAmount)
        assertEquals(entity.orderSummaryId, domain.orderSummaryId)
        assertEquals(entity.couponId, domain.couponId)
    }

    @Test
    @DisplayName("toEntity는 PaymentSummary를 PaymentSummaryJpaEntity로 변환한다")
    fun toEntity() {
        val domain =
            PaymentSummary(
                id = 20L,
                method = PaymentMethod.POINT,
                totalAmount = 20000,
                discountAmount = 2000,
                chargeAmount = 18000,
                orderSummaryId = 3L,
                couponId = 4L,
            )

        val entity = PaymentSummaryJpaEntityMapper.toEntity(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.method.name, entity.method.name)
        assertEquals(domain.totalAmount, entity.totalAmount)
        assertEquals(domain.discountAmount, entity.discountAmount)
        assertEquals(domain.chargeAmount, entity.chargeAmount)
        assertEquals(domain.orderSummaryId, entity.orderSummaryId)
        assertEquals(domain.couponId, entity.couponId)
    }
}
