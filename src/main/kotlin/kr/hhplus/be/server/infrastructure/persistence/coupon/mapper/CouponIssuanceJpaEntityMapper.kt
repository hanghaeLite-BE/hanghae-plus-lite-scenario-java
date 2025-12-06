package kr.hhplus.be.server.infrastructure.persistence.coupon.mapper

import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaEntity

internal object CouponIssuanceJpaEntityMapper {
    fun toEntity(domain: CouponIssuance): CouponIssuanceJpaEntity {
        return CouponIssuanceJpaEntity(
            couponSummaryId = domain.couponSummaryId,
            issuedCount = domain.issuedCount,
            maxCount = domain.maxCount,
            startAt = domain.startAt,
            endAt = domain.endAt,
        ).apply {
            id = domain.id
        }
    }

    fun toDomain(entity: CouponIssuanceJpaEntity): CouponIssuance {
        return CouponIssuance(
            id = entity.id,
            couponSummaryId = entity.couponSummaryId,
            issuedCount = entity.issuedCount,
            maxCount = entity.maxCount,
            startAt = entity.startAt,
            endAt = entity.endAt,
        )
    }
}
