package kr.hhplus.be.server.infrastructure.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
}
