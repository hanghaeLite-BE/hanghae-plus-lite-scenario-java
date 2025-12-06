package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.model.Payment.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByReservationId(Long reservationId);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

    // 멱등성 키를 통한 중복 결제 방지
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    // 결제 상태별 조회
    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusAndCreatedAtBetween(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 실패한 결제 재처리를 위한 조회
    List<Payment> findFailedPaymentsForRetry(LocalDateTime beforeDate);

    // 통계 조회
    Long countByStatusAndCreatedAtBetween(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}