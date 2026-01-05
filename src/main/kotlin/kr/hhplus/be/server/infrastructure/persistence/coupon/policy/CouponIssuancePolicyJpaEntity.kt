package kr.hhplus.be.server.infrastructure.persistence.coupon.policy

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaEntity

@Entity
@Table(name = "coupon_issuance_policy")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "policy_type", discriminatorType = DiscriminatorType.STRING)
sealed class CouponIssuancePolicyJpaEntity(
    @ManyToOne
    @JoinColumn(name = "coupon_issuance_id", nullable = false)
    val couponIssuanceJpaEntity: CouponIssuanceJpaEntity
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    override fun toString(): String {
        return "CouponIssuancePolicyJpaEntity(id=$id)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CouponIssuancePolicyJpaEntity) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
