package kr.hhplus.be.server.infrastructure.persistence.coupon.mapper

import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponSummaryJpaEntity

object CouponJpaEntityMapper {
    fun toDomain(
        entity: CouponJpaEntity,
        couponSummaryEntityToDomain: (CouponSummaryJpaEntity) -> CouponSummary
    ): Coupon =
        Coupon(
            id = entity.id,
            couponSummary = couponSummaryEntityToDomain(entity.couponSummaryJpaEntity),
            memberId = entity.memberId,
            usingAt = entity.usingAt,
            expiredAt = entity.expiredAt,
        )

    fun toEntity(
        domain: Coupon,
        couponSummaryDomainToEntity: (CouponSummary) -> CouponSummaryJpaEntity
    ): CouponJpaEntity =
        CouponJpaEntity(
            couponSummaryJpaEntity = couponSummaryDomainToEntity(domain.couponSummary),
            memberId = domain.memberId,
            expiredAt = domain.expiredAt,
        ).apply {
            id = domain.id
            usingAt = domain.usingAt
        }
}

