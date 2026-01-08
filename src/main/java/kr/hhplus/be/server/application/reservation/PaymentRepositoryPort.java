package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.reservation.Payment;

public interface PaymentRepositoryPort {
    Payment save(Payment payment);
}
