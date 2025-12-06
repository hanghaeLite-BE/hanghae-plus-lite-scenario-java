package kr.hhplus.be.server.infrastructure.persistence.product.mapper

import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.model.product.ProductSummary
import kr.hhplus.be.server.infrastructure.persistence.product.ProductSummaryJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class ProductSummaryJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 ProductSummaryJpaEntity를 ProductSummary로 변환한다")
    fun toDomain() {
        val entity = ProductSummaryJpaEntity(
            name = "상품명",
            price = 1000,
            stockQuantity = 10
        ).apply { id = 1L }

        val domain = ProductSummaryJpaEntityMapper.toDomain(entity)

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.price, domain.price)
        assertEquals(entity.stockQuantity, domain.stockQuantity)
    }

    @Test
    @DisplayName("toEntity는 ProductSummary를 ProductSummaryJpaEntity로 변환한다")
    fun toEntity() {
        val domain = ProductSummary(
            id = 2L,
            name = "다른상품",
            price = 2000,
            stockQuantity = 5
        )

        val entity = ProductSummaryJpaEntityMapper.toEntity(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.price, entity.price)
        assertEquals(domain.stockQuantity, entity.stockQuantity)
    }
}

