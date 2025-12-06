package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.UserBalance;
import kr.hhplus.be.server.domain.model.BalanceHistory;
import kr.hhplus.be.server.domain.model.TransactionType;
import kr.hhplus.be.server.domain.repository.UserBalanceRepository;
import kr.hhplus.be.server.infrastructure.persistence.entity.UserBalanceEntity;
import kr.hhplus.be.server.infrastructure.persistence.entity.BalanceHistoryEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserBalanceRepositoryImpl implements UserBalanceRepository {

    private final UserBalanceJpaRepository balanceJpaRepository;
    private final BalanceHistoryJpaRepository historyJpaRepository;

    public UserBalanceRepositoryImpl(UserBalanceJpaRepository balanceJpaRepository,
                                     BalanceHistoryJpaRepository historyJpaRepository) {
        this.balanceJpaRepository = balanceJpaRepository;
        this.historyJpaRepository = historyJpaRepository;
    }

    @Override
    @Transactional
    public UserBalance save(UserBalance balance) {
        UserBalanceEntity entity = toBalanceEntity(balance);
        UserBalanceEntity saved = balanceJpaRepository.save(entity);
        return toBalanceDomain(saved);
    }

    @Override
    public Optional<UserBalance> findByUserId(Long userId) {
        return balanceJpaRepository.findByUserId(userId)
                .map(this::toBalanceDomain);
    }

    @Override
    @Transactional
    public Optional<UserBalance> findByUserIdWithLock(Long userId) {
        return balanceJpaRepository.findByUserIdWithLock(userId)
                .map(this::toBalanceDomain);
    }

    @Override
    @Transactional
    public BalanceHistory saveHistory(BalanceHistory history) {
        BalanceHistoryEntity entity = toHistoryEntity(history);
        BalanceHistoryEntity saved = historyJpaRepository.save(entity);
        return toHistoryDomain(saved);
    }

    @Override
    public List<BalanceHistory> findHistoriesByUserId(Long userId) {
        return historyJpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toHistoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<BalanceHistory> findHistoriesByUserIdAndType(Long userId, TransactionType type) {
        return historyJpaRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type)
                .stream()
                .map(this::toHistoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndIdempotencyKey(Long userId, String idempotencyKey) {
        return historyJpaRepository.existsByUserIdAndIdempotencyKey(userId, idempotencyKey);
    }

    @Override
    public Optional<BalanceHistory> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey) {
        return historyJpaRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .map(this::toHistoryDomain);
    }

    @Override
    public List<UserBalance> findUsersWithBalanceGreaterThan(Long amount) {
        return balanceJpaRepository.findUsersWithBalanceGreaterThanEqual(amount)
                .stream()
                .map(this::toBalanceDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Long getTotalSystemBalance() {
        return balanceJpaRepository.sumAllBalances();
    }

    // Entity → Domain 변환 (UserBalance)
    private UserBalance toBalanceDomain(UserBalanceEntity entity) {
        UserBalance balance = UserBalance.of(
                entity.getUserId(),
                entity.getBalance(),
                entity.getLastUpdatedAt(),
                entity.getVersion()
        );
        balance.assignId(entity.getId());
        return balance;
    }

    // Domain → Entity 변환 (UserBalance)
    private UserBalanceEntity toBalanceEntity(UserBalance domain) {
        UserBalanceEntity entity = new UserBalanceEntity(
                domain.getUserId(),
                domain.getBalance(),
                domain.getLastUpdatedAt()
        );
        return entity;
    }

    // Entity → Domain 변환 (BalanceHistory)
    private BalanceHistory toHistoryDomain(BalanceHistoryEntity entity) {
        BalanceHistory history;

        if (entity.getType() == TransactionType.CHARGE) {
            history = BalanceHistory.createChargeHistory(
                    entity.getUserId(),
                    entity.getAmount(),
                    entity.getBalanceBefore(),
                    entity.getBalanceAfter(),
                    entity.getIdempotencyKey(),
                    entity.getDescription()
            );
        } else {
            history = BalanceHistory.createDeductHistory(
                    entity.getUserId(),
                    entity.getAmount(),
                    entity.getBalanceBefore(),
                    entity.getBalanceAfter(),
                    entity.getIdempotencyKey(),
                    entity.getDescription()
            );
        }

        history.assignId(entity.getId());
        return history;
    }

    // Domain → Entity 변환 (BalanceHistory)
    private BalanceHistoryEntity toHistoryEntity(BalanceHistory domain) {
        return new BalanceHistoryEntity(
                domain.getUserId(),
                domain.getType(),
                domain.getAmount(),
                domain.getBalanceBefore(),
                domain.getBalanceAfter(),
                domain.getDescription(),
                domain.getIdempotencyKey(),
                domain.getCreatedAt()
        );
    }
}