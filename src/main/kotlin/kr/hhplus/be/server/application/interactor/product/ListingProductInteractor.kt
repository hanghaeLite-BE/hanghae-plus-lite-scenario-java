package kr.hhplus.be.server.application.interactor.product

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.hhplus.be.server.application.enums.ListingProductDescending
import kr.hhplus.be.server.application.enums.ListingProductSortBy
import kr.hhplus.be.server.application.port.out.ListingProductOutput
import kr.hhplus.be.server.application.usecase.product.ListingProductUseCase
import kr.hhplus.be.server.application.vo.ListingProductVO
import kr.hhplus.be.server.application.vo.ProductSummaryItemVO
import kr.hhplus.be.server.application.vo.TopSellingProductItemVO
import kr.hhplus.be.server.application.vo.TopSellingProductVO
import kr.hhplus.be.server.domain.model.product.ProductSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Transactional(readOnly = true)
@Service
class ListingProductInteractor(
    private val listingProductOutput: ListingProductOutput,
) : ListingProductUseCase {
    private val logger = KotlinLogging.logger { }

    override fun listingBy(
        page: Int,
        size: Int,
        sortBy: String,
        descending: String,
    ): ListingProductVO {
        val listingProductSortBy =
            ListingProductSortBy.from(sortBy)
                ?: throw IllegalArgumentException("지원하지않는 정렬기준입니다($sortBy)")
        val listingProductDescending =
            ListingProductDescending.from(descending)
                ?: throw IllegalArgumentException("지원하지않는 정렬차순입니다($descending)")

        val productSummaries: List<ProductSummary> =
            listingProductOutput.listingBy(
                page = page,
                size = size,
                sortBy = listingProductSortBy,
                descending = listingProductDescending,
            )

        return ListingProductVO(
            rows = productSummaries.size,
            page = page,
            products =
                productSummaries.map {
                    ProductSummaryItemVO(
                        id = it.id!!,
                        name = it.name,
                        price = it.price,
                        stockQuantity = it.stockQuantity,
                    )
                },
        )
    }

    override fun topSellingProducts(
        nDay: Int,
        limit: Int,
        curDate: LocalDate,
    ): TopSellingProductVO {
        if (nDay <= 0 || limit <= 0) {
            logger.warn { "조회 기간 및 갯수는 0보다 커야합니다. nDay: $nDay, limit: $limit" }
            throw IllegalArgumentException("조회 기간 및 갯수는 0보다 커야합니다.")
        }
        val startDate = curDate.minusDays(nDay.toLong())

        return listingProductOutput
            .topSellingProducts(startDate, limit)
            .let { topSellingProducts ->
                val topSellingProductItemVOList =
                    topSellingProducts.map { (productSummary, totalOrderQuantity) ->
                        TopSellingProductItemVO(
                            id = productSummary.id!!,
                            name = productSummary.name,
                            price = productSummary.price,
                            stockQuantity = productSummary.stockQuantity,
                            totalOrderQuantity = totalOrderQuantity,
                        )
                    }
                TopSellingProductVO(products = topSellingProductItemVOList)
            }
    }
}
