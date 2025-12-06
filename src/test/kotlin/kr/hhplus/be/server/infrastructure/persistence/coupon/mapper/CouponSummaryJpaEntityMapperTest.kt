package kr.hhplus.be.server.infrastructure.persistence.coupon.mapper

import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponSummaryJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class CouponSummaryJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 CouponSummaryJpaEntity를 CouponSummary로 변환한다")
    fun toDomain() {
        val entity = CouponSummaryJpaEntity(
            name = "쿠폰",
            discountPercentage = 10,
            validDays = 30
        ).apply { id = 1L }

        val domain = CouponSummaryJpaEntityMapper.toDomain(entity)

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.discountPercentage, domain.discountPercentage)
        assertEquals(entity.validDays, domain.validDays)
    }

    @Test
    @DisplayName("toEntity는 CouponSummary를 CouponSummaryJpaEntity로 변환한다")
    fun toEntity() {
        val domain = CouponSummary(
            id = 2L,
            name = "다른쿠폰",
            discountPercentage = 20,
            validDays = 60
        )

        val entity = CouponSummaryJpaEntityMapper.toEntity(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.discountPercentage, entity.discountPercentage)
        assertEquals(domain.validDays, entity.validDays)
    }
}