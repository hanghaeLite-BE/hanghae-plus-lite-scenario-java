package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.infrastructure.persistence.entity.BalanceHistoryEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.UserBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface UserBalanceJpaRepository extends JpaRepository<UserBalanceEntity, Long> {

    Optional<UserBalanceEntity> findByUserId(Long userId);

    // 비관적 락으로 잔액 조회 (동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM UserBalanceEntity b WHERE b.userId = :userId")
    Optional<UserBalanceEntity> findByUserIdWithLock(@Param("userId") Long userId);

    // 특정 금액 이상 보유한 사용자들 조회
    @Query("SELECT b FROM UserBalanceEntity b WHERE b.balance >= :amount ORDER BY b.balance DESC")
    List<UserBalanceEntity> findUsersWithBalanceGreaterThanEqual(@Param("amount") Long amount);

    // 전체 시스템 잔액 합계 (통계용)
    @Query("SELECT COALESCE(SUM(b.balance), 0) FROM UserBalanceEntity b")
    Long sumAllBalances();

    // 잔액이 0인 사용자 수 조회
    @Query("SELECT COUNT(b) FROM UserBalanceEntity b WHERE b.balance = 0")
    Long countUsersWithZeroBalance();
}
