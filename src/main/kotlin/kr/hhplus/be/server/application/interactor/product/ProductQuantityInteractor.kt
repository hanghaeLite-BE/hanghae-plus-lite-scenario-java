package kr.hhplus.be.server.application.interactor.product

import kr.hhplus.be.server.application.port.out.ProductSummaryOutput
import kr.hhplus.be.server.application.usecase.product.ProductQuantityUseCase
import kr.hhplus.be.server.domain.model.order.OrderItem
import kr.hhplus.be.server.domain.model.product.ProductSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductQuantityInteractor(
    private val productSummaryOutput: ProductSummaryOutput,
) : ProductQuantityUseCase {
    /**
     * 주문된 상품들의 재고 감소
     */
    @Transactional
    override fun reduceBy(orderItems: List<OrderItem>): List<ProductSummary> {
        val productIdAndQuantityMap = orderItems.associate { it.productSummaryId to it.quantity }
        return productSummaryOutput
            .findAllInIds(productSummaryIds = productIdAndQuantityMap.keys)
            .map { it.reduceStockQuantity(productIdAndQuantityMap[it.id]!!) }
            .let { productSummaryOutput.saveAll(it) }
    }
}
