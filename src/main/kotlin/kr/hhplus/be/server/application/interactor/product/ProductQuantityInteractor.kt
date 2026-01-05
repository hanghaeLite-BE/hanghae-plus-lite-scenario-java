package kr.hhplus.be.server.application.interactor.product

import kr.hhplus.be.server.application.port.out.ProductSummaryOutput
import kr.hhplus.be.server.application.usecase.product.ProductQuantityUseCase
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.model.order.OrderItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductQuantityInteractor(
    private val productSummaryOutput: ProductSummaryOutput,
) : ProductQuantityUseCase {
    /**
     * 주문된 상품들의 재고 감소.
     * 빠르고 안전한 처리를 위해 조건부 UPDATE를 사용하여 동시성 이슈 방지한다.
     */
    @Transactional
    override fun reduceBy(orderItems: List<OrderItem>) =
        orderItems.forEach { orderItem ->
            if (!productSummaryOutput.reduceStock(orderItem.productSummaryId, orderItem.quantity)) {
                val product =
                    productSummaryOutput
                        .findAllInIds(
                            listOf(orderItem.productSummaryId),
                        ).firstOrNull()

                throw ConflictResourceException(
                    message = "'${product?.name ?: "알 수 없는 상품"}'의 재고가 부족합니다.",
                    clue =
                        mapOf(
                            "상품ID" to "${orderItem.productSummaryId}",
                            "재고" to "${product?.stockQuantity ?: "N/A"}",
                            "주문수량" to "${orderItem.quantity}",
                        ),
                )
            }
        }
}
