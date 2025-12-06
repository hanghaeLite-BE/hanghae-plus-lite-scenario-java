package kr.hhplus.be.server.infrastructure.persistence.coupon

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity

@Entity
@Table(name = "coupon_summary")
class CouponSummaryJpaEntity(
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val discountPercentage: Long,
    @Column
    val validDays: Int?,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
