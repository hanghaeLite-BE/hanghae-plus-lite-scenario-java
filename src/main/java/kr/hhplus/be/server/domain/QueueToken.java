package kr.hhplus.be.server.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class QueueToken {
    private Long id;
    private String userId;
    private String token;
    private Long position;
    private QueueStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime activatedAt;

    protected QueueToken() {
    }

    private QueueToken(String userId, String token, Long position, QueueStatus status) {
        this.userId = userId;
        this.token = token;
        this.position = position;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30); // 30분 후 만료
    }

    public static QueueToken createWaitingToken(String userId, Long position) {
        String token = generateToken(userId);
        return new QueueToken(userId, token, position, QueueStatus.WAITING);
    }

    public static QueueToken createActiveToken(String userId) {
        String token = generateToken(userId);
        QueueToken queueToken = new QueueToken(userId, token, 0L, QueueStatus.ACTIVE);
        queueToken.activatedAt = LocalDateTime.now();
        queueToken.expiresAt = LocalDateTime.now().plusMinutes(10); // 활성 토큰은 10분
        return queueToken;
    }

    private static String generateToken(String userId) {
        return "queue_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public void activate() {
        if (this.status != QueueStatus.WAITING) {
            throw new IllegalStateException("대기 중인 토큰만 활성화할 수 있습니다.");
        }
        this.status = QueueStatus.ACTIVE;
        this.position = 0L;
        this.activatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(10);
    }

    public void expire() {
        this.status = QueueStatus.EXPIRED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt) || status == QueueStatus.EXPIRED;
    }

    public boolean isActive() {
        return status == QueueStatus.ACTIVE && !isExpired();
    }

    public long getEstimatedWaitTimeMinutes() {
        if (status == QueueStatus.ACTIVE) {
            return 0;
        }
        // 1분에 10명씩 처리된다고 가정
        return (position / 10) + 1;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Long getPosition() {
        return position;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    void setId(Long id) {
        this.id = id;
    }

    void updatePosition(Long newPosition) {
        this.position = newPosition;
    }
}