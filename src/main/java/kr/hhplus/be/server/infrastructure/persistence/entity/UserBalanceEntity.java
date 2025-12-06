package kr.hhplus.be.server.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_balances")
public class UserBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long balance;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Version // 낙관적 락
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // JPA용 기본 생성자
    protected UserBalanceEntity() {}

    public UserBalanceEntity(Long userId, Long balance, LocalDateTime lastUpdatedAt) {
        this.userId = userId;
        this.balance = balance;
        this.lastUpdatedAt = lastUpdatedAt;
        this.createdAt = LocalDateTime.now();
        this.version = 0L;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getBalance() { return balance; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public Long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setBalance(Long balance) { this.balance = balance; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
