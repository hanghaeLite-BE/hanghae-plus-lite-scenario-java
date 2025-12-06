package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;

public class BalanceHistory {
    private Long id;
    private final Long userId;
    private final TransactionType type;
    private final Long amount;
    private final Long balanceBefore;
    private final Long balanceAfter;
    private final String description;
    private final String idempotencyKey;
    private final LocalDateTime createdAt;

    private BalanceHistory(Long userId, TransactionType type, Long amount,
                           Long balanceBefore, Long balanceAfter, String description,
                           String idempotencyKey, LocalDateTime createdAt) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    // 팩토리 메서드 - 충전 히스토리
    public static BalanceHistory createChargeHistory(Long userId, Long amount,
                                                     Long balanceBefore, Long balanceAfter,
                                                     String idempotencyKey, String description) {
        return new BalanceHistory(
                userId,
                TransactionType.CHARGE,
                amount,
                balanceBefore,
                balanceAfter,
                description,
                idempotencyKey,
                LocalDateTime.now()
        );
    }

    // 팩토리 메서드 - 차감 히스토리
    public static BalanceHistory createDeductHistory(Long userId, Long amount,
                                                     Long balanceBefore, Long balanceAfter,
                                                     String idempotencyKey, String description) {
        return new BalanceHistory(
                userId,
                TransactionType.DEDUCT,
                amount,
                balanceBefore,
                balanceAfter,
                description,
                idempotencyKey,
                LocalDateTime.now()
        );
    }

    // ID 할당 (Infrastructure 레이어에서만 사용)
    void assignId(Long id) {
        this.id = id;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public TransactionType getType() { return type; }
    public Long getAmount() { return amount; }
    public Long getBalanceBefore() { return balanceBefore; }
    public Long getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

