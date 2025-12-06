package kr.hhplus.be.server.infrastructure.persistence.coupon.mapper

import io.mockk.mockk
import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponSummaryJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CouponJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 CouponJpaEntity를 Coupon으로 변환한다")
    fun toDomainCouponJpaEntity() {
        val couponSummaryEntity = mockk<CouponSummaryJpaEntity>()
        val couponSummary = mockk<CouponSummary>()
        val entity = CouponJpaEntity(
            couponSummaryJpaEntity = couponSummaryEntity,
            memberId = 1L,
            expiredAt = LocalDateTime.now()
        ).apply {
            id = 10L
            usingAt = LocalDateTime.now().plusDays(1)
        }
        val couponSummaryEntityToDomain: (CouponSummaryJpaEntity) -> CouponSummary = { couponSummary }

        val domain = CouponJpaEntityMapper.toDomain(entity, couponSummaryEntityToDomain)

        assertEquals(entity.id, domain.id)
        assertEquals(couponSummary, domain.couponSummary)
        assertEquals(entity.memberId, domain.memberId)
        assertEquals(entity.usingAt, domain.usingAt)
        assertEquals(entity.expiredAt, domain.expiredAt)
    }

    @Test
    @DisplayName("toEntity는 Coupon을 CouponJpaEntity로 변환한다")
    fun toEntityCouponJpaEntity() {
        val couponSummary = mockk<CouponSummary>()
        val couponSummaryEntity = mockk<CouponSummaryJpaEntity>()
        val domain = Coupon(
            id = 20L,
            couponSummary = couponSummary,
            memberId = 2L,
            usingAt = LocalDateTime.now(),
            expiredAt = LocalDateTime.now().plusDays(2)
        )
        val couponSummaryDomainToEntity: (CouponSummary) -> CouponSummaryJpaEntity = { couponSummaryEntity }

        val entity = CouponJpaEntityMapper.toEntity(domain, couponSummaryDomainToEntity)

        assertEquals(domain.id, entity.id)
        assertEquals(couponSummaryEntity, entity.couponSummaryJpaEntity)
        assertEquals(domain.memberId, entity.memberId)
        assertEquals(domain.usingAt, entity.usingAt)
        assertEquals(domain.expiredAt, entity.expiredAt)
    }
}

