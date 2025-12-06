package kr.hhplus.be.server.application.usecase.product

import kr.hhplus.be.server.application.vo.ListingProductVO
import kr.hhplus.be.server.application.vo.TopSellingProductVO
import java.time.LocalDate

interface ListingProductUseCase {
    fun listingBy(
        page: Int,
        size: Int,
        sortBy: String,
        descending: String,
    ): ListingProductVO

    fun topSellingProducts(
        nDay: Int,
        limit: Int,
        curDate: LocalDate = LocalDate.now(),
    ): TopSellingProductVO
}
