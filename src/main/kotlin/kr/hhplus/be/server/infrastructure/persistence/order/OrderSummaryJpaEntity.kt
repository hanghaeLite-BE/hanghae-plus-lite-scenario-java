package kr.hhplus.be.server.infrastructure.persistence.order

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity

@Entity
@Table(name = "order_summary")
class OrderSummaryJpaEntity(
    @Column(nullable = false)
    val memberId: Long,
    @OneToMany(mappedBy = "orderSummaryId", cascade = [CascadeType.ALL])
    val orderItems: MutableList<OrderItemJpaEntity>,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    constructor(memberId: Long) : this(
        memberId = memberId,
        orderItems = mutableListOf(),
    )

    fun addOrderItems(orderItemList: List<OrderItemJpaEntity>): OrderSummaryJpaEntity {
        this.orderItems.addAll(orderItemList)
        return this
    }
}
