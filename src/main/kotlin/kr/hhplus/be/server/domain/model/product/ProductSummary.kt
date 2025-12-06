package kr.hhplus.be.server.domain.model.product

import kr.hhplus.be.server.domain.exception.ConflictResourceException

data class ProductSummary(
    val id: Long?,
    val name: String,
    val price: Int,
    val stockQuantity: Int,
) {
    fun reduceStockQuantity(quantity: Int): ProductSummary {
        if (quantity <= 0) throw IllegalArgumentException("주문수량은 0개보다 많아야합니다")
        if (stockQuantity < quantity) {
            throw ConflictResourceException(
                message = "'$name'의 재고가 부족합니다.",
                clue = mapOf("상품ID" to "$id", "재고" to "$stockQuantity", "주문수량" to "$quantity"),
            )
        }

        return copy(
            id = id,
            name = name,
            price = price,
            stockQuantity = stockQuantity - quantity,
        )
    }

    fun increaseStockQuantity(quantity: Int): ProductSummary {
        if (quantity <= 0) throw IllegalArgumentException("추가수량은 0개보다 많아야합니다")

        return copy(
            id = id,
            name = name,
            price = price,
            stockQuantity = stockQuantity + quantity,
        )
    }
}
