package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public class Payment {
    private final Long id;
    private final Long reservationId;
    private final Long userId;
    private final Long amount;
    private final LocalDateTime paidAt;

    public Payment(Long id, Long reservationId, Long userId, Long amount, LocalDateTime paidAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.userId = userId;
        this.amount = amount;
        this.paidAt = paidAt;
    }

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public Long getUserId() { return userId; }
    public Long getAmount() { return amount; }
    public LocalDateTime getPaidAt() { return paidAt; }

    public static class PaymentBuilder {
        private Long id;
        private Long reservationId;
        private Long userId;
        private Long amount;
        private LocalDateTime paidAt;

        public PaymentBuilder id(Long id) { this.id = id; return this; }
        public PaymentBuilder reservationId(Long reservationId) { this.reservationId = reservationId; return this; }
        public PaymentBuilder userId(Long userId) { this.userId = userId; return this; }
        public PaymentBuilder amount(Long amount) { this.amount = amount; return this; }
        public PaymentBuilder paidAt(LocalDateTime paidAt) { this.paidAt = paidAt; return this; }

        public Payment build() {
            return new Payment(id, reservationId, userId, amount, paidAt);
        }
    }
}
