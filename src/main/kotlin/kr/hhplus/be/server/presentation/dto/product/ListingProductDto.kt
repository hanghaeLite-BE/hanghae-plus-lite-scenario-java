package kr.hhplus.be.server.presentation.dto.product

import kr.hhplus.be.server.application.vo.ListingProductVO
import kr.hhplus.be.server.application.vo.TopSellingProductVO

data class ListingProductResponse(
    val rows: Int,
    val page: Int,
    val products: List<ProductSummaryItem>,
) {
    constructor(vo: ListingProductVO) : this(
        rows = vo.rows,
        page = vo.page,
        products =
            vo.products.map {
                ProductSummaryItem(
                    id = it.id,
                    name = it.name,
                    price = it.price,
                    stockQuantity = it.stockQuantity,
                )
            },
    )
}

data class ProductSummaryItem(
    val id: Long,
    val name: String,
    val price: Int,
    val stockQuantity: Int,
)

data class TopSellingProductListResponse(
    val count: Int,
    val products: List<TopSellingProductItem>,
) {
    constructor(vo: TopSellingProductVO) : this(
        count = vo.products.size,
        products =
            vo.products.map { topSellingProduct ->
                TopSellingProductItem(
                    id = topSellingProduct.id,
                    name = topSellingProduct.name,
                    price = topSellingProduct.price,
                    stockQuantity = topSellingProduct.stockQuantity,
                    totalOrderQuantity = topSellingProduct.totalOrderQuantity,
                )
            },
    )
}

data class TopSellingProductItem(
    val id: Long,
    val name: String,
    val price: Int,
    val stockQuantity: Int,
    val totalOrderQuantity: Int,
)
