package kr.hhplus.be.server.infrastructure.persistence.coupon.policy

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaEntity

@Entity
@DiscriminatorValue("ONE_PER_MEMBER")
class OnePerMemberPolicyJpaEntity(
    couponIssuanceJpaEntity: CouponIssuanceJpaEntity
) : CouponIssuancePolicyJpaEntity(couponIssuanceJpaEntity)
