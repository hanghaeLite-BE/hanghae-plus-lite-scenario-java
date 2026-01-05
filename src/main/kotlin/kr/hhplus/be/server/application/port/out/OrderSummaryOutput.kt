package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.order.OrderSummary
import java.util.Optional

interface OrderSummaryOutput {
    fun save(domain: OrderSummary): OrderSummary

    fun findById(id: Long): Optional<OrderSummary>

    fun findByIdWithOrderItems(id: Long): Optional<OrderSummary>
}
