package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.enums.ListingProductDescending
import kr.hhplus.be.server.application.enums.ListingProductSortBy
import kr.hhplus.be.server.common.annotation.IntegrationTest
import kr.hhplus.be.server.common.config.TestDataSourceConfig
import kr.hhplus.be.server.common.config.TestDockerComposeContainer
import kr.hhplus.be.server.domain.model.product.ProductSummary
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup

@IntegrationTest
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup(
    Sql(scripts = ["/sql/listing-product-setup.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/listing-product-cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
@Import(
    ProductSummaryPersistenceAdapter::class,
    TestDockerComposeContainer::class,
    TestDataSourceConfig::class,
)
class ProductSummaryPersistenceAdapterIntegrationTest {
    @Autowired
    private lateinit var adapter: ProductSummaryPersistenceAdapter

    @Nested
    @DisplayName("상품 리스트 조회")
    inner class ListingProducts {
        @Test
        @DisplayName("최근 등록된 순으로 상품 리스트를 조회한다")
        fun listingProducts() {
            // when
            val productSummaries =
                adapter.listingBy(
                    page = 0,
                    size = 10,
                    sortBy = ListingProductSortBy.REGISTER,
                    descending = ListingProductDescending.DESC,
                )
            // then
            assertThat(productSummaries).hasSize(10)
            val expected =
                listOf(
                    ProductSummary(3L, "LG OLED TV 65인치", 10000, 20),
                    ProductSummary(7L, "Nintendo Switch OLED", 3000, 90),
                    ProductSummary(1L, "Apple iPhone 15 Pro", 7000, 30),
                    ProductSummary(8L, "Canon EOS R6 Mark II", 6000, 70),
                    ProductSummary(5L, "Apple iPad Pro 12.9", 1000, 60),
                    ProductSummary(10L, "Apple Watch Ultra 2", 5000, 50),
                    ProductSummary(4L, "Dyson V15 무선청소기", 4000, 80),
                    ProductSummary(6L, "Sony WH-1000XM5 헤드폰", 8000, 10),
                    ProductSummary(9L, "Bose QuietComfort Ultra", 9000, 40),
                    ProductSummary(2L, "Samsung Galaxy S24 Ultra", 2000, 100),
                )
            productSummaries.forEachIndexed { idx, summary ->
                assertThat(summary).isEqualTo(expected[idx])
            }
        }

        @Test
        @DisplayName("오래된 등록된 순으로 상품 리스트를 조회한다")
        fun listingProducts_orderByOldest() {
            // when
            val productSummaries =
                adapter.listingBy(
                    page = 0,
                    size = 10,
                    sortBy = ListingProductSortBy.REGISTER,
                    descending = ListingProductDescending.ASC,
                )
            // then
            assertThat(productSummaries).hasSize(10)
            val expected =
                listOf(
                    ProductSummary(2L, "Samsung Galaxy S24 Ultra", 2000, 100),
                    ProductSummary(9L, "Bose QuietComfort Ultra", 9000, 40),
                    ProductSummary(6L, "Sony WH-1000XM5 헤드폰", 8000, 10),
                    ProductSummary(4L, "Dyson V15 무선청소기", 4000, 80),
                    ProductSummary(10L, "Apple Watch Ultra 2", 5000, 50),
                    ProductSummary(5L, "Apple iPad Pro 12.9", 1000, 60),
                    ProductSummary(8L, "Canon EOS R6 Mark II", 6000, 70),
                    ProductSummary(1L, "Apple iPhone 15 Pro", 7000, 30),
                    ProductSummary(7L, "Nintendo Switch OLED", 3000, 90),
                    ProductSummary(3L, "LG OLED TV 65인치", 10000, 20),
                )
            productSummaries.forEachIndexed { idx, summary ->
                assertThat(summary).isEqualTo(expected[idx])
            }
        }

        @Test
        @DisplayName("높은 가격순으로 상품 리스트를 조회한다")
        fun listingProducts_orderByHighPrice() {
            // when
            val productSummaries =
                adapter.listingBy(
                    page = 0,
                    size = 10,
                    sortBy = ListingProductSortBy.PRICE,
                    descending = ListingProductDescending.DESC,
                )
            // then
            assertThat(productSummaries).hasSize(10)
            val expected =
                listOf(
                    ProductSummary(3L, "LG OLED TV 65인치", 10000, 20),
                    ProductSummary(9L, "Bose QuietComfort Ultra", 9000, 40),
                    ProductSummary(6L, "Sony WH-1000XM5 헤드폰", 8000, 10),
                    ProductSummary(1L, "Apple iPhone 15 Pro", 7000, 30),
                    ProductSummary(8L, "Canon EOS R6 Mark II", 6000, 70),
                    ProductSummary(10L, "Apple Watch Ultra 2", 5000, 50),
                    ProductSummary(4L, "Dyson V15 무선청소기", 4000, 80),
                    ProductSummary(7L, "Nintendo Switch OLED", 3000, 90),
                    ProductSummary(2L, "Samsung Galaxy S24 Ultra", 2000, 100),
                    ProductSummary(5L, "Apple iPad Pro 12.9", 1000, 60),
                )
            productSummaries.forEachIndexed { idx, summary ->
                assertThat(summary).isEqualTo(expected[idx])
            }
        }

        @Test
        @DisplayName("낮은 가격순으로 상품 리스트를 조회한다")
        fun listingProducts_orderByLowPrice() {
            // when
            val productSummaries =
                adapter.listingBy(
                    page = 0,
                    size = 10,
                    sortBy = ListingProductSortBy.PRICE,
                    descending = ListingProductDescending.ASC,
                )
            // then
            assertThat(productSummaries).hasSize(10)
            val expected =
                listOf(
                    ProductSummary(5L, "Apple iPad Pro 12.9", 1000, 60),
                    ProductSummary(2L, "Samsung Galaxy S24 Ultra", 2000, 100),
                    ProductSummary(7L, "Nintendo Switch OLED", 3000, 90),
                    ProductSummary(4L, "Dyson V15 무선청소기", 4000, 80),
                    ProductSummary(10L, "Apple Watch Ultra 2", 5000, 50),
                    ProductSummary(8L, "Canon EOS R6 Mark II", 6000, 70),
                    ProductSummary(1L, "Apple iPhone 15 Pro", 7000, 30),
                    ProductSummary(6L, "Sony WH-1000XM5 헤드폰", 8000, 10),
                    ProductSummary(9L, "Bose QuietComfort Ultra", 9000, 40),
                    ProductSummary(3L, "LG OLED TV 65인치", 10000, 20),
                )
            productSummaries.forEachIndexed { idx, summary ->
                assertThat(summary).isEqualTo(expected[idx])
            }
        }
    }
}
