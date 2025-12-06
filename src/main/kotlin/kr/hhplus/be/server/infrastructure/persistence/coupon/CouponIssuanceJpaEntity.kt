package kr.hhplus.be.server.infrastructure.persistence.coupon

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity
import java.time.LocalDateTime

@Entity
@Table(name = "coupon_issuance")
class CouponIssuanceJpaEntity(
    @Column(nullable = false)
    val couponSummaryId: Long,
    @Column(nullable = false)
    val issuedCount: Int,
    @Column
    val maxCount: Int?,
    @Column(nullable = false)
    val startAt: LocalDateTime,
    @Column(nullable = false)
    val endAt: LocalDateTime,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
