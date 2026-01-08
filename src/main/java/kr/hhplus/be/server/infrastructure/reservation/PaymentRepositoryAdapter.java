package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.application.reservation.PaymentRepositoryPort;
import kr.hhplus.be.server.domain.reservation.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setReservationId(payment.getReservationId());
        entity.setUserId(payment.getUserId());
        entity.setAmount(payment.getAmount());
        entity.setPaidAt(payment.getPaidAt());
        
        PaymentEntity saved = paymentJpaRepository.save(entity);
        
        return Payment.builder()
                .id(saved.getId())
                .reservationId(saved.getReservationId())
                .userId(saved.getUserId())
                .amount(saved.getAmount())
                .paidAt(saved.getPaidAt())
                .build();
    }
}
