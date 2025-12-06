package kr.hhplus.be.server.infrastructure.persistence.entity;

import kr.hhplus.be.server.domain.model.TransactionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance_histories")
public class BalanceHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "balance_before", nullable = false)
    private Long balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(length = 500)
    private String description;

    @Column(name = "idempotency_key", nullable = false, length = 36)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // JPA용 기본 생성자
    protected BalanceHistoryEntity() {}

    public BalanceHistoryEntity(Long userId, TransactionType type, Long amount,
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

    // Getters and Setters
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