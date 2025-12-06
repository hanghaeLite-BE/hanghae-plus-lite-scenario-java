package kr.hhplus.be.server.infrastructure.persistence.coupon.mapper

import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CouponIssuanceJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 CouponIssuanceJpaEntity를 CouponIssuance로 변환한다")
    fun toDomainCouponIssuanceJpaEntity() {
        val entity = CouponIssuanceJpaEntity(
            couponSummaryId = 1L,
            issuedCount = 2,
            maxCount = 10,
            startAt = LocalDateTime.now(),
            endAt = LocalDateTime.now().plusDays(1)
        ).apply { id = 100L }

        val domain = CouponIssuanceJpaEntityMapper.toDomain(entity)

        assertEquals(entity.id, domain.id)
        assertEquals(entity.couponSummaryId, domain.couponSummaryId)
        assertEquals(entity.issuedCount, domain.issuedCount)
        assertEquals(entity.maxCount, domain.maxCount)
        assertEquals(entity.startAt, domain.startAt)
        assertEquals(entity.endAt, domain.endAt)
    }

    @Test
    @DisplayName("toEntity는 CouponIssuance를 CouponIssuanceJpaEntity로 변환한다")
    fun toEntityCouponIssuanceJpaEntity() {
        val domain = CouponIssuance(
            id = 200L,
            couponSummaryId = 2L,
            issuedCount = 3,
            maxCount = 20,
            startAt = LocalDateTime.now(),
            endAt = LocalDateTime.now().plusDays(2)
        )

        val entity = CouponIssuanceJpaEntityMapper.toEntity(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.couponSummaryId, entity.couponSummaryId)
        assertEquals(domain.issuedCount, entity.issuedCount)
        assertEquals(domain.maxCount, entity.maxCount)
        assertEquals(domain.startAt, entity.startAt)
        assertEquals(domain.endAt, entity.endAt)
    }
}
