package kr.hhplus.be.server.domain.model.order

data class OrderItem(
    val id: Long?,
    val orderSummaryId: Long,
    val productSummaryId: Long,
    val quantity: Int,
    val price: Int,
)
