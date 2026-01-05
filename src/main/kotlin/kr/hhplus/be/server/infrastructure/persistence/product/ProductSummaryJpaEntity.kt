package kr.hhplus.be.server.infrastructure.persistence.product

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity

@Entity
@Table(name = "product_summary")
class ProductSummaryJpaEntity(
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val price: Int,
    @Column(nullable = false) var stockQuantity: Int,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
