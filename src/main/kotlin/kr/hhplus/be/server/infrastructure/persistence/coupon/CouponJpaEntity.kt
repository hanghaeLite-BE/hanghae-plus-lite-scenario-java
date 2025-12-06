package kr.hhplus.be.server.infrastructure.persistence.coupon

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity
import java.time.LocalDateTime

@Entity
@Table(name = "coupon")
class CouponJpaEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_summary_id", nullable = false)
    val couponSummaryJpaEntity: CouponSummaryJpaEntity,
    @Column(nullable = false)
    val memberId: Long,
    @Column
    val expiredAt: LocalDateTime?,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = true)
    var usingAt: LocalDateTime? = null

    override fun toString(): String = "CouponJpaEntity(id=$id, memberId=$memberId, expiredAt=$expiredAt, usingAt=$usingAt)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CouponJpaEntity) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
