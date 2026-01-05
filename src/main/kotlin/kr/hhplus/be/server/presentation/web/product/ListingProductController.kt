package kr.hhplus.be.server.presentation.web.product

import kr.hhplus.be.server.application.usecase.product.ListingProductUseCase
import kr.hhplus.be.server.presentation.dto.product.ListingProductResponse
import kr.hhplus.be.server.presentation.dto.product.TopSellingProductListResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ListingProductController(
    private val listingProductUseCase: ListingProductUseCase,
) {
    @GetMapping
    fun listingProducts(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "register") sortBy: String,
        @RequestParam(required = false, defaultValue = "desc") descending: String,
    ): ListingProductResponse {
        val listingProductVO =
            listingProductUseCase.listingBy(
                page = page,
                size = size,
                sortBy = sortBy,
                descending = descending,
            )

        return ListingProductResponse(listingProductVO)
    }

    /**
     * 최근 n일간 가장 많이 팔린 상위 m개 상품 정보를 제공하는 API 를 작성합니다.
     */
    @GetMapping("/top-selling")
    fun listingTopSellingProducts(
        @RequestParam(required = false, defaultValue = "3") nDay: Int,
        @RequestParam(required = false, defaultValue = "5") mProduct: Int,
    ): TopSellingProductListResponse {
        val topSellingProductVO =
            listingProductUseCase
                .topSellingProducts(
                    nDay = nDay,
                    limit = mProduct,
                )

        return TopSellingProductListResponse(topSellingProductVO)
    }
}
