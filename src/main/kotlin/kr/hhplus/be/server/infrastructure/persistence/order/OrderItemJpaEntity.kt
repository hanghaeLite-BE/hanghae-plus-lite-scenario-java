package kr.hhplus.be.server.infrastructure.persistence.order

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity

@Entity
@Table(name = "order_item")
class OrderItemJpaEntity(
    @Column(nullable = false)
    val orderSummaryId: Long,
    @Column(nullable = false)
    val productSummaryId: Long,
    @Column(nullable = false)
    val quantity: Int,
    @Column(nullable = false)
    val price: Int,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
