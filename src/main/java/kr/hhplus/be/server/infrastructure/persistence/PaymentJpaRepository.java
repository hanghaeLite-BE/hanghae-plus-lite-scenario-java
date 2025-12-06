package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.Payment.PaymentStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByReservationId(Long reservationId);

    List<PaymentEntity> findByUserId(Long userId);

    List<PaymentEntity> findByUserIdAndStatus(Long userId, PaymentStatus status);

    // 멱등성 키 관련 조회
    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentEntity> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    // 상태별 조회
    List<PaymentEntity> findByStatus(PaymentStatus status);

    List<PaymentEntity> findByStatusAndCreatedAtBetween(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 실패한 결제 재시도용 조회
    @Query("""
        SELECT p FROM PaymentEntity p 
        WHERE p.status = 'FAILED' 
        AND p.createdAt < :beforeDate 
        ORDER BY p.createdAt ASC
        """)
    List<PaymentEntity> findFailedPaymentsForRetry(@Param("beforeDate") LocalDateTime beforeDate);

    // 통계 조회
    Long countByStatusAndCreatedAtBetween(
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 사용자별 최근 결제 내역
    @Query("""
        SELECT p FROM PaymentEntity p 
        WHERE p.userId = :userId 
        ORDER BY p.createdAt DESC 
        LIMIT :limit
        """)
    List<PaymentEntity> findRecentPaymentsByUserId(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    // 특정 기간 동안의 결제 통계
    @Query("""
        SELECT p.status, COUNT(p), SUM(p.amount) 
        FROM PaymentEntity p 
        WHERE p.createdAt BETWEEN :startDate AND :endDate 
        GROUP BY p.status
        """)
    List<Object[]> getPaymentStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
