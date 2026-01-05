package kr.hhplus.be.server.application.vo

data class ListingProductVO(
    val rows: Int,
    val page: Int,
    val products: List<ProductSummaryItemVO>,
)

data class ProductSummaryItemVO(
    val id: Long,
    val name: String,
    val price: Int,
    val stockQuantity: Int,
)

data class TopSellingProductVO(
    val products: List<TopSellingProductItemVO>,
)

data class TopSellingProductItemVO(
    val id: Long,
    val name: String,
    val price: Int,
    val stockQuantity: Int,
    val totalOrderQuantity: Int,
)
