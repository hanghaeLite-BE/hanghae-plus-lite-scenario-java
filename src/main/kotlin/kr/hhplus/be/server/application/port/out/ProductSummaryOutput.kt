package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.product.ProductSummary

interface ProductSummaryOutput {
    fun findAllInIds(productSummaryIds: Collection<Long>): List<ProductSummary>

    fun saveAll(updatedProductSummaryList: Collection<ProductSummary>): List<ProductSummary>

    /**
     * 재고를 원자적으로 차감합니다
     */
    fun reduceStock(
        productSummaryId: Long,
        quantity: Int,
    ): Boolean
}
