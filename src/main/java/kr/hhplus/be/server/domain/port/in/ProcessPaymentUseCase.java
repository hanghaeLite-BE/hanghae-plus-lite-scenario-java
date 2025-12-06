package kr.hhplus.be.server.domain.port.in;

import kr.hhplus.be.server.domain.model.Payment;

public interface ProcessPaymentUseCase {
    Payment processPayment(ProcessPaymentCommand command);

    class ProcessPaymentCommand {
        private final Long reservationId;
        private final Long userId;
        private final Long amount;

        public ProcessPaymentCommand(Long reservationId, Long userId, Long amount) {
            this.reservationId = reservationId;
            this.userId = userId;
            this.amount = amount;
        }

        public Long getReservationId() { return reservationId; }
        public Long getUserId() { return userId; }
        public Long getAmount() { return amount; }
    }
}
