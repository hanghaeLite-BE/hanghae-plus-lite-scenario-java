package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.model.order.OrderSummary
import kr.hhplus.be.server.domain.utils.StubFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@UnitTest
class OrderSummaryTest {
    @Nested
    @DisplayName("주문 생성")
    inner class PlaceOrderTest {
        @Test
        @DisplayName("id가 null이고, memberId와 orderItems가 올바르게 설정된 OrderSummary를 반환한다")
        fun returnsOrderSummaryWithNullIdAndGivenMemberIdAndOrderItems() {
            val memberId = 1L
            val orderItems = listOf(
                StubFactory.orderItem(id = 1L, orderSummaryId = 0L, productSummaryId = 100L, quantity = 2, price = 5000),
                StubFactory.orderItem(id = 2L, orderSummaryId = 0L, productSummaryId = 200L, quantity = 1, price = 10000)
            )
            val orderSummary = OrderSummary.placeOrder(memberId, orderItems)
            assertThat(orderSummary.id).isNull()
            assertThat(orderSummary.memberId).isEqualTo(memberId)
            assertThat(orderSummary.orderItems).isEqualTo(orderItems)
        }
    }

    @Nested
    @DisplayName("총 주문 금액 계산")
    inner class GetTotalAmountTest {
        @Test
        @DisplayName("getTotalAmount는 모든 orderItem의 (price * quantity) 합을 반환한다")
        fun returnsSumOfPriceTimesQuantityForAllOrderItems() {
            val orderItems = listOf(
                StubFactory.orderItem(id = 1L, orderSummaryId = 0L, productSummaryId = 100L, quantity = 2, price = 5000),
                StubFactory.orderItem(id = 2L, orderSummaryId = 0L, productSummaryId = 200L, quantity = 1, price = 10000)
            )
            val orderSummary = StubFactory.orderSummary(id = 10L, memberId = 1L, orderItems = orderItems)
            assertThat(orderSummary.getTotalAmount()).isEqualTo(5000 * 2 + 10000 * 1)
        }
    }

    @Nested
    @DisplayName("주문 아이템 추가")
    inner class AddOrderItemsTest {
        @Test
        @DisplayName("기존 orderItems에 새 orderItems를 추가한 새 OrderSummary를 반환한다")
        fun returnsNewOrderSummaryWithAddedOrderItems() {
            val originalItems = listOf(
                StubFactory.orderItem(id = 1L, orderSummaryId = 0L, productSummaryId = 100L, quantity = 1, price = 1000)
            )
            val newItems = listOf(
                StubFactory.orderItem(id = 2L, orderSummaryId = 0L, productSummaryId = 200L, quantity = 2, price = 2000)
            )
            val orderSummary = StubFactory.orderSummary(id = 10L, memberId = 1L, orderItems = originalItems)
            val updated = orderSummary.addOrderItems(newItems)
            assertThat(updated.orderItems).containsExactlyElementsOf(originalItems + newItems)
            assertThat(updated.id).isEqualTo(orderSummary.id)
            assertThat(updated.memberId).isEqualTo(orderSummary.memberId)
        }
    }
}
