package kr.hhplus.be.server.infrastructure.persistence.entity;

import kr.hhplus.be.server.domain.model.QueueStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_tokens")
public class QueueTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_value", unique = true, nullable = false, length = 36)
    private String tokenValue;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private QueueStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "position")
    private Integer position;

    @Column(name = "entered_at")
    private LocalDateTime enteredAt;

    // JPA용 기본 생성자
    protected QueueTokenEntity() {}

    public QueueTokenEntity(String tokenValue, Long userId, Long concertId,
                            QueueStatus status, LocalDateTime createdAt,
                            LocalDateTime expiresAt, Integer position,
                            LocalDateTime enteredAt) {
        this.tokenValue = tokenValue;
        this.userId = userId;
        this.concertId = concertId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.position = position;
        this.enteredAt = enteredAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getTokenValue() { return tokenValue; }
    public Long getUserId() { return userId; }
    public Long getConcertId() { return concertId; }
    public QueueStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public Integer getPosition() {
        return null;
    }
    public LocalDateTime getEnteredAt() { return enteredAt; }

    public void setStatus(QueueStatus status) { this.status = status; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setPosition(Integer position) { this.position = position; }
    public void setEnteredAt(LocalDateTime enteredAt) { this.enteredAt = enteredAt; }

    public void setId(Long id) {
    }
}
