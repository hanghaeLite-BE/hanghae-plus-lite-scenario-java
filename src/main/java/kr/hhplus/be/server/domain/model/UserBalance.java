package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;

public class UserBalance {
    private Long id;
    private final Long userId;
    private final Long balance;
    private final LocalDateTime lastUpdatedAt;
    private final Long version; // 낙관적 락을 위한 버전

    private UserBalance(Long userId, Long balance, LocalDateTime lastUpdatedAt, Long version) {
        if (balance < 0) {
            throw new IllegalArgumentException("잔액은 음수가 될 수 없습니다.");
        }
        this.userId = userId;
        this.balance = balance;
        this.lastUpdatedAt = lastUpdatedAt;
        this.version = version;
    }

    // 팩토리 메서드 - 신규 사용자 잔액 생성
    public static UserBalance createNew(Long userId) {
        return new UserBalance(userId, 0L, LocalDateTime.now(), 0L);
    }

    // 팩토리 메서드 - 기존 잔액으로 생성 (Infrastructure에서 사용)
    public static UserBalance of(Long userId, Long balance, LocalDateTime lastUpdatedAt, Long version) {
        return new UserBalance(userId, balance, lastUpdatedAt, version);
    }

    // 비즈니스 로직 - 충전
    public UserBalance charge(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        Long newBalance = this.balance + amount;
        return new UserBalance(this.userId, newBalance, LocalDateTime.now(), this.version + 1);
    }

    // 비즈니스 로직 - 차감
    public UserBalance deduct(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }

        if (this.balance < amount) {
            throw new IllegalArgumentException("잔액이 부족합니다. 현재 잔액: " + this.balance + ", 차감 요청: " + amount);
        }

        Long newBalance = this.balance - amount;
        return new UserBalance(this.userId, newBalance, LocalDateTime.now(), this.version + 1);
    }

    // 비즈니스 로직 - 차감 가능 여부 확인
    public boolean canDeduct(Long amount) {
        return this.balance >= amount;
    }

    // ID 할당 (Infrastructure 레이어에서만 사용)
    void assignId(Long id) {
        this.id = id;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getBalance() { return balance; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public Long getVersion() { return version; }
}
