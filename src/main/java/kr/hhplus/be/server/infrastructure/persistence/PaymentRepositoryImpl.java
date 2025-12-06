package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.model.Payment.PaymentStatus;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        PaymentEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByReservationId(Long reservationId) {
        return jpaRepository.findByReservationId(reservationId)
                .map(this::toDomain);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status) {
        return jpaRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey) {
        return jpaRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .map(this::toDomain);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByStatusAndCreatedAtBetween(PaymentStatus status,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate) {
        return jpaRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findFailedPaymentsForRetry(LocalDateTime beforeDate) {
        return jpaRepository.findFailedPaymentsForRetry(beforeDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByStatusAndCreatedAtBetween(PaymentStatus status,
                                                 LocalDateTime startDate,
                                                 LocalDateTime endDate) {
        return jpaRepository.countByStatusAndCreatedAtBetween(status, startDate, endDate);
    }


    private PaymentEntity toEntity(Payment domain) {
        return new PaymentEntity(
                domain.getReservationId(),
                domain.getUserId(),
                domain.getAmount(),
                domain.getStatus(),
                domain.getIdempotencyKey(),
                domain.getPaymentMethod(),
                domain.getTransactionId(),
                domain.getFailureReason(),
                domain.getPaidAt()
        );
    }

    private Payment toDomain(PaymentEntity entity) {
        // 엔티티의 상태에 따라 적절한 도메인 객체 생성
        Payment domain;

        if (entity.getReservationId() != null) {
            // 예약 ID가 있는 경우
            domain = Payment.createWithReservation(
                    entity.getReservationId(),
                    entity.getUserId(),
                    entity.getAmount(),
                    entity.getPaymentMethod() != null ? entity.getPaymentMethod() : "DEFAULT",
                    entity.getIdempotencyKey()
            );
        } else {
            // 예약 ID가 없는 경우
            domain = Payment.createPending(
                    entity.getUserId(),
                    entity.getAmount(),
                    entity.getPaymentMethod() != null ? entity.getPaymentMethod() : "DEFAULT",
                    entity.getIdempotencyKey()
            );
        }

        // ID 할당
        domain = domain.withId(entity.getId());

        // 상태에 따른 변환
        if (entity.getStatus() == PaymentStatus.COMPLETED && entity.getTransactionId() != null) {
            domain = domain.complete(entity.getTransactionId());
        } else if (entity.getStatus() == PaymentStatus.COMPLETED) {
            domain = domain.complete();
        } else if (entity.getStatus() == PaymentStatus.FAILED && entity.getFailureReason() != null) {
            domain = domain.fail(entity.getFailureReason());
        }

        return domain;
    }

}