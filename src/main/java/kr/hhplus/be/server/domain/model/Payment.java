package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;

public class Payment {
    private Long id;
    private final Long reservationId;
    private final Long userId;
    private final Long amount;
    private final PaymentStatus status;
    private final LocalDateTime paidAt;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }

    private Payment(Long reservationId, Long userId, Long amount) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.paidAt = LocalDateTime.now();
    }

    public static Payment create(Long reservationId, Long userId, Long amount) {
        return new Payment(reservationId, userId, amount);
    }

    // 비즈니스 규칙: 결제 완료 처리
    public Payment complete() {
        return new Payment(this.reservationId, this.userId, this.amount,
                PaymentStatus.COMPLETED, this.paidAt, this.id);
    }

    private Payment(Long reservationId, Long userId, Long amount,
                    PaymentStatus status, LocalDateTime paidAt, Long id) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
        this.id = id;
    }

    void assignId(Long id) {
        this.id = id;
    }

    // Getters
    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public Long getUserId() { return userId; }
    public Long getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
