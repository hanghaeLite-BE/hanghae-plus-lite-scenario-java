package kr.hhplus.be.server.infrastructure.persistence;
import kr.hhplus.be.server.infrastructure.persistence.entity.BalanceHistoryEntity;
import kr.hhplus.be.server.domain.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BalanceHistoryJpaRepository extends JpaRepository<BalanceHistoryEntity, Long> {

    // 사용자별 히스토리 조회 (최신순)
    List<BalanceHistoryEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 사용자별 + 타입별 히스토리 조회
    List<BalanceHistoryEntity> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, TransactionType type);

    // 멱등성 키로 중복 체크
    boolean existsByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    // 멱등성 키로 기존 거래 조회
    Optional<BalanceHistoryEntity> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    // 특정 기간 거래 내역 조회
    @Query("SELECT h FROM BalanceHistoryEntity h WHERE h.userId = :userId " +
            "AND h.createdAt BETWEEN :startDate AND :endDate ORDER BY h.createdAt DESC")
    List<BalanceHistoryEntity> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    // 일별 거래 통계 (관리자용)
    @Query("SELECT DATE(h.createdAt) as date, h.type, COUNT(h), SUM(h.amount) " +
            "FROM BalanceHistoryEntity h " +
            "WHERE h.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(h.createdAt), h.type ORDER BY date DESC")
    List<Object[]> getDailyTransactionStats(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
}