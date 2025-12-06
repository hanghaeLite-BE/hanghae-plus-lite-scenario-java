package kr.hhplus.be.server.domain.model;

public class User {
    private Long id;
    private final String userId;
    private final Long balance;

    private User(String userId, Long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public static User create(String userId, Long initialBalance) {
        return new User(userId, initialBalance);
    }

    // 비즈니스 규칙: 잔액 확인
    public boolean hasEnoughBalance(Long amount) {
        return balance >= amount;
    }

    // 비즈니스 규칙: 잔액 차감
    public User deductBalance(Long amount) {
        if (!hasEnoughBalance(amount)) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        return new User(this.userId, this.balance - amount, this.id);
    }

    public User chargeBalance(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        return new User(this.userId, this.balance + amount, this.id);
    }

    private User(String userId, Long balance, Long id) {
        this.userId = userId;
        this.balance = balance;
        this.id = id;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    // Getters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public Long getBalance() { return balance; }
}
