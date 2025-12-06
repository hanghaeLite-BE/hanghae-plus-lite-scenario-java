package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;

public class Payment {
    private Long id;
    private final Long reservationId;
    private final Long userId;
    private final Long amount;
    private final PaymentStatus status;
    private final LocalDateTime paidAt;
    private final String idempotencyKey;
    private final String paymentMethod;
    private final String transactionId;
    private final String failureReason;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }

    private Payment(Long id, Long reservationId, Long userId, Long amount,
                    PaymentStatus status, LocalDateTime paidAt, String idempotencyKey,
                    String paymentMethod, String transactionId, String failureReason) {
        this.id = id;
        this.reservationId = reservationId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
        this.idempotencyKey = idempotencyKey;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.failureReason = failureReason;
    }

    public static Payment create(Long reservationId, Long userId, Long amount) {
        return new Payment(
                null,                    // id
                reservationId,          // reservationId
                userId,                 // userId
                amount,                 // amount
                PaymentStatus.PENDING,  // status
                LocalDateTime.now(),    // paidAt
                null,                   // idempotencyKey
                "DEFAULT",              // paymentMethod
                null,                   // transactionId
                null                    // failureReason
        );
    }

    public static Payment createPending(Long userId, Long amount,
                                        String paymentMethod, String idempotencyKey) {
        return new Payment(
                null,                    // id
                null,                    // reservationId
                userId,                  // userId
                amount,                  // amount
                PaymentStatus.PENDING,   // status
                LocalDateTime.now(),     // paidAt
                idempotencyKey,          // idempotencyKey
                paymentMethod,           // paymentMethod
                null,                    // transactionId
                null                     // failureReason
        );
    }

    public static Payment createWithReservation(Long reservationId, Long userId, Long amount,
                                                String paymentMethod, String idempotencyKey) {
        return new Payment(
                null,                    // id
                reservationId,           // reservationId
                userId,                  // userId
                amount,                  // amount
                PaymentStatus.PENDING,   // status
                LocalDateTime.now(),     // paidAt
                idempotencyKey,          // idempotencyKey
                paymentMethod,           // paymentMethod
                null,                    // transactionId
                null                     // failureReason
        );
    }

    public Payment complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 결제만 완료할 수 있습니다.");
        }

        return new Payment(
                this.id,                 // id 유지
                this.reservationId,      // reservationId 유지
                this.userId,             // userId 유지
                this.amount,             // amount 유지
                PaymentStatus.COMPLETED, // status 변경
                LocalDateTime.now(),     // paidAt 업데이트
                this.idempotencyKey,     // idempotencyKey 유지
                this.paymentMethod,      // paymentMethod 유지
                null,                    // transactionId (기존 버전에서는 null)
                null                     // failureReason
        );
    }

    public Payment complete(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 결제만 완료할 수 있습니다.");
        }

        return new Payment(
                this.id,                 // id 유지
                this.reservationId,      // reservationId 유지
                this.userId,             // userId 유지
                this.amount,             // amount 유지
                PaymentStatus.COMPLETED, // status 변경
                LocalDateTime.now(),     // paidAt 업데이트
                this.idempotencyKey,     // idempotencyKey 유지
                this.paymentMethod,      // paymentMethod 유지
                transactionId,           // transactionId 설정
                null                     // failureReason
        );
    }

    public Payment fail(String failureReason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 결제만 실패 처리할 수 있습니다.");
        }

        return new Payment(
                this.id,                // id 유지
                this.reservationId,     // reservationId 유지
                this.userId,            // userId 유지
                this.amount,            // amount 유지
                PaymentStatus.FAILED,   // status 변경
                this.paidAt,            // paidAt 유지 (실패 시 원본 시간 보존)
                this.idempotencyKey,    // idempotencyKey 유지
                this.paymentMethod,     // paymentMethod 유지
                this.transactionId,     // transactionId 유지
                failureReason           // failureReason 설정
        );
    }

    public void assignId(Long id) {
        // reflection을 사용하여 final 필드에 값 할당하거나
        // 새로운 객체를 생성하여 반환하는 방식으로 구현
        if (this.id == null) {
            try {
                java.lang.reflect.Field idField = this.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(this, id);
            } catch (Exception e) {
                throw new RuntimeException("ID 할당 실패", e);
            }
        }
    }

    // ===== ID가 포함된 새 객체 생성 (더 안전한 방법) =====
    public Payment withId(Long id) {
        return new Payment(
                id,                     // 새 id
                this.reservationId,     // 기존 값들 유지
                this.userId,
                this.amount,
                this.status,
                this.paidAt,
                this.idempotencyKey,
                this.paymentMethod,
                this.transactionId,
                this.failureReason
        );
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    public boolean canBeCompleted() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean canBeRetried() {
        return this.status == PaymentStatus.FAILED;
    }

    // ===== Getters =====
    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public Long getUserId() { return userId; }
    public Long getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public String getFailureReason() { return failureReason; }

    @Override
    public String toString() {
        return String.format(
                "Payment{id=%d, reservationId=%d, userId=%d, amount=%d, status=%s, idempotencyKey='%s'}",
                id, reservationId, userId, amount, status, idempotencyKey
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Payment payment = (Payment) obj;
        return id != null && id.equals(payment.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
