package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.application.enums.ListingProductDescending
import kr.hhplus.be.server.application.enums.ListingProductSortBy
import kr.hhplus.be.server.domain.model.product.ProductSummary
import java.time.LocalDate

interface ListingProductOutput {
    fun listingBy(
        page: Int,
        size: Int,
        sortBy: ListingProductSortBy,
        descending: ListingProductDescending,
    ): List<ProductSummary>

    fun topSellingProducts(
        startDate: LocalDate,
        limit: Int,
    ): List<Pair<ProductSummary, Int>>
}
