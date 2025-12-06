package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.enums.ListingProductDescending
import kr.hhplus.be.server.application.enums.ListingProductSortBy
import kr.hhplus.be.server.application.port.out.ListingProductOutput
import kr.hhplus.be.server.application.port.out.ProductSummaryOutput
import kr.hhplus.be.server.domain.model.product.ProductSummary
import kr.hhplus.be.server.infrastructure.persistence.product.ProductSummaryJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.product.mapper.ProductSummaryJpaEntityMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class ProductSummaryPersistenceAdapter(
    private val productSummaryRepository: ProductSummaryJpaRepository,
) : ProductSummaryOutput,
    ListingProductOutput {
    override fun findAllInIds(productSummaryIds: Collection<Long>): List<ProductSummary> =
        productSummaryRepository
            .findAllByIdIn(productSummaryIds)
            .map(ProductSummaryJpaEntityMapper::toDomain)

    override fun saveAll(updatedProductSummaryList: Collection<ProductSummary>): List<ProductSummary> {
        val updatedProductSummaryJpaEntityList =
            updatedProductSummaryList
                .map(ProductSummaryJpaEntityMapper::toEntity)

        return productSummaryRepository
            .saveAll(updatedProductSummaryJpaEntityList)
            .map(ProductSummaryJpaEntityMapper::toDomain)
    }

    override fun listingBy(
        page: Int,
        size: Int,
        sortBy: ListingProductSortBy,
        descending: ListingProductDescending,
    ): List<ProductSummary> {
        val pageable =
            PageRequest.of(
                page,
                size,
                generateSort(sortBy, descending),
            )

        return productSummaryRepository
            .findAll(pageable)
            .content
            .map(ProductSummaryJpaEntityMapper::toDomain)
    }

    private fun generateSort(
        sortBy: ListingProductSortBy,
        descending: ListingProductDescending,
    ): Sort {
        val sort =
            when (sortBy) {
                ListingProductSortBy.REGISTER -> Sort.by(Sort.Order.asc("createdAt"))
                ListingProductSortBy.PRICE -> Sort.by(Sort.Order.asc("price"))
            }
        return if (ListingProductDescending.DESC == descending) {
            sort.descending()
        } else {
            sort.ascending()
        }
    }

    /**
     * 최근 n일간 가장 많이 팔린 상위 m개 상품 정보를 제공한다
     */
    override fun topSellingProducts(
        startDate: LocalDate,
        limit: Int,
    ): List<Pair<ProductSummary, Int>> {
        val topSellingProductSummary =
            productSummaryRepository.topSellingProductsInNDays(
                startAt = LocalDateTime.of(startDate, LocalTime.MIN),
                limit = limit,
            )

        return topSellingProductSummary.map {
            ProductSummary(
                id = it.getId(),
                name = it.getName(),
                price = it.getPrice(),
                stockQuantity = it.getStockQuantity(),
            ) to it.getTotalOrderCount()
        }
    }
}
