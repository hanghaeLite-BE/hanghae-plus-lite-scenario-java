package kr.hhplus.be.server.domain.port.out;

import kr.hhplus.be.server.domain.model.Payment;

public interface PaymentGateway {
    Payment processPayment(Payment payment);
}
