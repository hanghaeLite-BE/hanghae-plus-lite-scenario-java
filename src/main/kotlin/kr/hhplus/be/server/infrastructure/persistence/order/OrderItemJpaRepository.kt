package kr.hhplus.be.server.infrastructure.persistence.order

import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemJpaRepository : JpaRepository<OrderItemJpaEntity, Long>
