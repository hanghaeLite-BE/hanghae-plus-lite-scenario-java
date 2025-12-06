package kr.hhplus.be.server.presentation.web.product

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kr.hhplus.be.server.common.annotation.IntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import java.time.Clock
import java.time.Instant

@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@SqlGroup(
    Sql(scripts = ["/sql/listing-product-setup.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/listing-product-cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class ListingProductControllerIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val fixedInstant = Instant.parse("2025-09-19T15:00:00Z")
        val fixedClock = Clock.fixed(fixedInstant, Clock.systemDefaultZone().zone)

        mockkStatic(Clock::class)
        every { Clock.systemDefaultZone() } returns fixedClock
    }

    @AfterEach
    fun teardown() {
        unmockkStatic(Clock::class)
    }

    @Nested
    @DisplayName("상품 목록 조회 API [GET /api/v1/products]")
    inner class ListingProducts {
        @Test
        @DisplayName("상품 목록을 조회한다.")
        fun listingProductsTest() {
            // given
            val memberId = 1L

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/products")
                        .header("X-Member-Id", memberId)
                        .queryParams(
                            LinkedMultiValueMap(
                                mapOf(
                                    "page" to listOf("0"),
                                    "size" to listOf("10"),
                                ),
                            ),
                        ),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.rows").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.page").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("$.products.length()").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].id").value(3L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[1].id").value(7L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[2].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[3].id").value(8L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[4].id").value(5L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[5].id").value(10L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[6].id").value(4L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[7].id").value(6L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[8].id").value(9L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[9].id").value(2L))
        }
    }

    @Nested
    @DisplayName("최근 n일간 가장 많이 팔린 상위 m개 상품 조회 API [GET /api/v1/products/top-selling]")
    inner class ListingTopSellingProductTest {
        @Test
        @DisplayName("최근 3일간 가장 많이 팔린 상위 5개 상품을 조회한다.")
        fun listingTopSellingProductsTest() {
            // given
            val nDay = 3
            val mProduct = 5

            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/products/top-selling")
                        .queryParams(
                            LinkedMultiValueMap(
                                mapOf(
                                    "nDay" to listOf(nDay.toString()),
                                    "mProduct" to listOf(mProduct.toString()),
                                ),
                            ),
                        ),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products").isArray)
                .andExpect(MockMvcResultMatchers.jsonPath("$.products.length()").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].id").value(4L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[0].totalOrderQuantity").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[1].id").value(3L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[1].totalOrderQuantity").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[2].id").value(10L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[2].totalOrderQuantity").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[3].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[3].totalOrderQuantity").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[4].id").value(9L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.products[4].totalOrderQuantity").value(1))
        }

        @ParameterizedTest
        @DisplayName("n 또는 m이 0 이하인 경우 예외가 발생한다.")
        @CsvSource(
            "0, 5",
            "3, 0",
            "-1, 5",
            "3, -1",
            "0, 0",
            "-1, -1",
        )
        fun listingTopSellingProducts_invalidParameter_exception(
            nDay: Int,
            mProduct: Int,
        ) {
            // when & then
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get("/api/v1/products/top-selling")
                        .queryParams(
                            LinkedMultiValueMap(
                                mapOf(
                                    "nDay" to listOf(nDay.toString()),
                                    "mProduct" to listOf(mProduct.toString()),
                                ),
                            ),
                        ),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("조회 기간 및 갯수는 0보다 커야합니다."))
        }
    }
}
