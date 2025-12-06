package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.utils.StubFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class ProductSummaryTest {
    @Nested
    @DisplayName("잔여수량 차감")
    inner class ReduceStockTest {
        @DisplayName("잔여수량이 10개일때 3개 차감하면 잔여수량이 7개가 된다.")
        @Test
        fun reduceStockReducesQuantity() {
            // given
            val productSummary = StubFactory.productSummary(id = 1L, name = "마이부", price = 10, stockQuantity = 10)

            // when
            val updatedSummary = productSummary.reduceStockQuantity(3)

            // then
            assertThat(updatedSummary.stockQuantity).isEqualTo(7)
        }

        @DisplayName("차감 수량이 0개 이하인 경우 예외 발생")
        @Test
        fun reduceStockInvalidQuantityThrowsException() {
            // given
            val productSummary = StubFactory.productSummary(id = 2L, name = "마이부", price = 10, stockQuantity = 5)

            // when
            val exception = assertThrows<IllegalArgumentException> { productSummary.reduceStockQuantity(0) }

            // then
            assertThat(exception.message).isEqualTo("주문수량은 0개보다 많아야합니다")
        }

        @DisplayName("차감 수량이 잔여수량보다 큰 경우 예외 발생")
        @Test
        fun reduceStockExceedingQuantityThrowsException() {
            // given
            val productSummary = StubFactory.productSummary(id = 3L, name = "마이부", price = 10, stockQuantity = 2)

            // when
            val exception = assertThrows<ConflictResourceException> { productSummary.reduceStockQuantity(5) }

            // then
            assertThat(exception.message).isEqualTo("'마이부'의 재고가 부족합니다.")
        }
    }

    @Nested
    @DisplayName("잔여수량 추가")
    inner class IncreaseStockTest {
        @DisplayName("잔여수량이 10개일때 3개 추가하면 잔여수량이 13개가 된다.")
        @Test
        fun increaseStockIncreasesQuantity() {
            // given
            val productSummary = StubFactory.productSummary(id = 1L, name = "마이부", price = 10, stockQuantity = 10)

            // when
            val updatedSummary = productSummary.increaseStockQuantity(3)

            // then
            assertThat(updatedSummary.stockQuantity).isEqualTo(13)
        }

        @DisplayName("추가 수량이 0개 이하인 경우 예외 발생")
        @Test
        fun increaseStockInvalidQuantityThrowsException() {
            // given
            val productSummary = StubFactory.productSummary(id = 2L, name = "마이부", price = 10, stockQuantity = 5)

            // when
            val exception = assertThrows<IllegalArgumentException> { productSummary.increaseStockQuantity(0) }

            // then
            assertThat(exception.message).isEqualTo("추가수량은 0개보다 많아야합니다")
        }
    }
}
