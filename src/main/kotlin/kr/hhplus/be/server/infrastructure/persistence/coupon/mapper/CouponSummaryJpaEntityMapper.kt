package kr.hhplus.be.server.infrastructure.persistence.coupon.mapper

import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponSummaryJpaEntity

object CouponSummaryJpaEntityMapper {
    fun toDomain(entity: CouponSummaryJpaEntity): CouponSummary =
        CouponSummary(
            id = entity.id,
            name = entity.name,
            discountPercentage = entity.discountPercentage,
            validDays = entity.validDays,
        )

    fun toEntity(domain: CouponSummary): CouponSummaryJpaEntity =
        CouponSummaryJpaEntity(
            name = domain.name,
            discountPercentage = domain.discountPercentage,
            validDays = domain.validDays,
        ).apply {
            id = domain.id
        }
}
