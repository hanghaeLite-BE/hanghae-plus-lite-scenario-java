package kr.hhplus.be.server.application.interactor.product

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit
import java.time.LocalDate

@Transactional(readOnly = true)
@Service
class ListingProductInteractor(
    private val listingProductOutput: ListingProductOutput,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : ListingProductUseCase {
    private val logger = KotlinLogging.logger { }

    /**
     * 상품 목록 조회
     * 조회 성능 개선을 위해 Redis 캐싱을 적용했습니다.
     * TTL 기반으로 5분간 캐시를 유지합니다.
     *
     * 주의: 상품 정보 변경 시 캐시 무효화 로직이 없습니다.
     * TTL 만료 전까지는 변경된 정보가 반영되지 않을 수 있습니다.
     */
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

        // 캐시 키 생성
        val cacheKey = "product:list:$page:$size:$sortBy:$descending"

        // 캐시에서 조회 시도
        val cached = redisTemplate.opsForValue().get(cacheKey)
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, ListingProductVO::class.java)
            } catch (e: Exception) {
                logger.warn(e) { "캐시 역직렬화 실패, DB에서 조회합니다. key: $cacheKey" }
            }
        }

        // 캐시 미스 시 DB에서 조회
        val productSummaries: List<ProductSummary> =
            listingProductOutput.listingBy(
                page = page,
                size = size,
                sortBy = listingProductSortBy,
                descending = listingProductDescending,
            )

        val result = ListingProductVO(
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

        // 캐시에 저장 (TTL 5분)
        try {
            val serialized = objectMapper.writeValueAsString(result)
            redisTemplate.opsForValue().set(
                cacheKey,
                serialized,
                5,
                TimeUnit.MINUTES,
            )
        } catch (e: Exception) {
            logger.warn(e) { "캐시 저장 실패, 계속 진행합니다. key: $cacheKey" }
        }

        return result
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
