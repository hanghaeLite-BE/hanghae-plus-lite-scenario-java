package kr.hhplus.be.server.infrastructure.persistence.payment

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity

@Entity
@Table(name = "payment_summary")
class PaymentSummaryJpaEntity(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val method: PaymentMethod,
    @Column(nullable = false)
    val totalAmount: Long,
    @Column(nullable = false)
    val discountAmount: Long,
    @Column(nullable = false)
    val chargeAmount: Long,
    @Column(nullable = false)
    val memberId: Long,
    @Column(nullable = false)
    val orderSummaryId: Long,
    @Column
    val couponId: Long?,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

enum class PaymentMethod {
    POINT,
    CARD,
    PHONE,
    BANK_TRANSFER,
    VIRTUAL_ACCOUNT,
}
