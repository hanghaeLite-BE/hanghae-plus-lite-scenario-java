package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.UserBalance;
import kr.hhplus.be.server.domain.model.BalanceHistory;
import kr.hhplus.be.server.domain.model.TransactionType;
import java.util.List;
import java.util.Optional;

public interface UserBalanceRepository {
    UserBalance save(UserBalance balance);
    Optional<UserBalance> findByUserId(Long userId);

    // 동시성 제어를 위한 비관적 락
    Optional<UserBalance> findByUserIdWithLock(Long userId);

    // 잔액 히스토리 관리
    BalanceHistory saveHistory(BalanceHistory history);
    List<BalanceHistory> findHistoriesByUserId(Long userId);
    List<BalanceHistory> findHistoriesByUserIdAndType(Long userId, TransactionType type);

    // 멱등성 보장을 위한 중복 체크
    boolean existsByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
    Optional<BalanceHistory> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    // 통계 및 관리
    List<UserBalance> findUsersWithBalanceGreaterThan(Long amount);
    Long getTotalSystemBalance();
}
