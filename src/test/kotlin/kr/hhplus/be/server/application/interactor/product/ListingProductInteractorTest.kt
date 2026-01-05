package kr.hhplus.be.server.application.interactor.product

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.enums.ListingProductDescending
import kr.hhplus.be.server.application.enums.ListingProductSortBy
import kr.hhplus.be.server.application.port.out.ListingProductOutput
import kr.hhplus.be.server.application.vo.ProductSummaryItemVO
import kr.hhplus.be.server.application.vo.TopSellingProductItemVO
import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.utils.StubFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@UnitTest
class ListingProductInteractorTest {
    @Nested
    @DisplayName("상품 목록 조회")
    inner class ListingByTest {
        private val listingProductOutputMock = mockk<ListingProductOutput>()
        private val listingProductInteractor = ListingProductInteractor(listingProductOutput = listingProductOutputMock)

        @Test
        @DisplayName("상품 목록이 정상적으로 반환된다")
        fun listingBy() {
            // given
            val page = 0
            val size = 2
            val sortBy = "REGISTER"
            val descending = "DESC"
            val productSummaries =
                listOf(
                    StubFactory.productSummary(
                        id = 1L,
                        name = "Apple iPhone 15 Pro",
                        price = 1700000,
                        stockQuantity = 5,
                    ),
                    StubFactory.productSummary(
                        id = 2L,
                        name = "Samsung Galaxy S24 Ultra",
                        price = 1600000,
                        stockQuantity = 3,
                    ),
                )
            every {
                listingProductOutputMock.listingBy(
                    page = page,
                    size = size,
                    sortBy = ListingProductSortBy.REGISTER,
                    descending = ListingProductDescending.DESC,
                )
            } returns productSummaries

            // when
            val result = listingProductInteractor.listingBy(page, size, sortBy, descending)

            // then
            assertThat(result.rows).isEqualTo(2)
            assertThat(result.page).isEqualTo(page)
            assertThat(result.products).containsExactly(
                ProductSummaryItemVO(
                    id = 1L,
                    name = "Apple iPhone 15 Pro",
                    price = 1700000,
                    stockQuantity = 5,
                ),
                ProductSummaryItemVO(
                    id = 2L,
                    name = "Samsung Galaxy S24 Ultra",
                    price = 1600000,
                    stockQuantity = 3,
                ),
            )
        }

        @Test
        @DisplayName("지원하지 않는 정렬 기준이면 예외가 발생한다")
        fun listingBy_notSupportedSortBy() {
            // given
            val page = 0
            val size = 2
            val sortBy = "NOT_EXIST"
            val descending = "DESC"

            // when & then
            assertThatThrownBy {
                listingProductInteractor.listingBy(page, size, sortBy, descending)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("지원하지않는 정렬기준입니다")
        }

        @Test
        @DisplayName("지원하지 않는 정렬 차순이면 예외가 발생한다")
        fun listingBy_notSupportedDescending() {
            // given
            val page = 0
            val size = 2
            val sortBy = "REGISTER"
            val descending = "NOT_EXIST"

            // when & then
            assertThatThrownBy {
                listingProductInteractor.listingBy(page, size, sortBy, descending)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("지원하지않는 정렬차순입니다")
        }
    }

    @Nested
    @DisplayName("가장 많이 팔린 상품 조회")
    inner class TopSellingProductsTest {
        private val listingProductOutputMock = mockk<ListingProductOutput>()
        private val listingProductInteractor = ListingProductInteractor(listingProductOutput = listingProductOutputMock)

        @Test
        @DisplayName("가장 많이 팔린 상품이 정상적으로 반환된다")
        fun topSellingProducts() {
            // given
            val nDay = 3
            val limit = 2
            val topSellingProducts =
                listOf(
                    Pair(
                        StubFactory.productSummary(
                            id = 1L,
                            name = "Apple iPhone 15 Pro",
                            price = 1700000,
                            stockQuantity = 5,
                        ),
                        10,
                    ),
                    Pair(
                        StubFactory.productSummary(
                            id = 2L,
                            name = "Samsung Galaxy S24 Ultra",
                            price = 1600000,
                            stockQuantity = 3,
                        ),
                        8,
                    ),
                )
            every {
                listingProductOutputMock.topSellingProducts(
                    startDate = any(),
                    limit = limit,
                )
            } returns topSellingProducts

            // when
            val result = listingProductInteractor.topSellingProducts(nDay, limit)

            // then
            assertThat(result.products).hasSize(2)
            assertThat(result.products).containsExactly(
                TopSellingProductItemVO(
                    id = 1L,
                    name = "Apple iPhone 15 Pro",
                    price = 1700000,
                    stockQuantity = 5,
                    totalOrderQuantity = 10,
                ),
                TopSellingProductItemVO(
                    id = 2L,
                    name = "Samsung Galaxy S24 Ultra",
                    price = 1600000,
                    stockQuantity = 3,
                    totalOrderQuantity = 8,
                ),
            )
        }

        @Test
        @DisplayName("기간 또는 갯수가 0 이하이면 예외가 발생한다")
        fun topSellingProducts_invalidInput() {
            // given
            val invalidInputs =
                listOf(
                    Pair(0, 5),
                    Pair(3, 0),
                    Pair(-1, 5),
                    Pair(3, -1),
                )

            for ((nDay, mProduct) in invalidInputs) {
                // when & then
                assertThatThrownBy {
                    listingProductInteractor.topSellingProducts(nDay, mProduct)
                }.isInstanceOf(
                    IllegalArgumentException::class.java,
                ).hasMessageContaining("조회 기간 및 갯수는 0보다 커야합니다.")
            }
        }
    }
}
