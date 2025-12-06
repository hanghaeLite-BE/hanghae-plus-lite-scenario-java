package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.product.ProductSummary

interface ProductSummaryOutput {
    fun findAllInIds(productSummaryIds: Collection<Long>): List<ProductSummary>

    fun saveAll(updatedProductSummaryList: Collection<ProductSummary>): List<ProductSummary>
}
