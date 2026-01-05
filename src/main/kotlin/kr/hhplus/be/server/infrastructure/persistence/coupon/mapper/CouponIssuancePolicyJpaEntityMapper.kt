package kr.hhplus.be.server.infrastructure.persistence.coupon.mapper

import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import kr.hhplus.be.server.domain.model.coupon.policy.CouponIssuancePolicy
import kr.hhplus.be.server.domain.model.coupon.policy.OnePerMemberPolicy
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.coupon.policy.CouponIssuancePolicyJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.coupon.policy.OnePerMemberPolicyJpaEntity

internal object CouponIssuancePolicyJpaEntityMapper {
    fun toDomain(
        entity: CouponIssuancePolicyJpaEntity,
        couponIssuanceEntityTo: (CouponIssuanceJpaEntity) -> CouponIssuance,
    ): CouponIssuancePolicy {
        return when (entity) {
            is OnePerMemberPolicyJpaEntity -> OnePerMemberPolicy(
                id = entity.id,
                couponIssuance = couponIssuanceEntityTo(entity.couponIssuanceJpaEntity),
            )
        }
    }
}