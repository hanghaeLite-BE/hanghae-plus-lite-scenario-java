package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class QueueToken {
    private Long id;
    private final String tokenValue;
    private final Long userId;
    private final Long concertId;
    private final QueueStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final Integer position;
    private final LocalDateTime enteredAt;

    private QueueToken(String tokenValue, Long userId, Long concertId,
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

    // 팩토리 메서드 - 새 대기열 토큰 생성
    public static QueueToken createWaitingToken(Long userId, Long concertId) {
        return new QueueToken(
                UUID.randomUUID().toString(),
                userId,
                concertId,
                QueueStatus.WAITING,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1), // 1시간 후 만료
                null, // position은 별도로 설정
                null
        );
    }

    // 대기열 진입 (ACTIVE 상태로 변경)
    public QueueToken activate() {
        if (status != QueueStatus.WAITING) {
            throw new IllegalStateException("대기 중인 토큰만 활성화할 수 있습니다.");
        }

        return new QueueToken(
                this.tokenValue,
                this.userId,
                this.concertId,
                QueueStatus.ACTIVE,
                this.createdAt,
                LocalDateTime.now().plusMinutes(10), // 10분간 활성
                this.position,
                LocalDateTime.now()
        );
    }

    // 토큰 만료
    public QueueToken expire() {
        return new QueueToken(
                this.tokenValue,
                this.userId,
                this.concertId,
                QueueStatus.EXPIRED,
                this.createdAt,
                this.expiresAt,
                this.position,
                this.enteredAt
        );
    }

    // 토큰 완료 (예약 완료 후)
    public QueueToken complete() {
        if (status != QueueStatus.ACTIVE) {
            throw new IllegalStateException("활성 토큰만 완료할 수 있습니다.");
        }

        return new QueueToken(
                this.tokenValue,
                this.userId,
                this.concertId,
                QueueStatus.COMPLETED,
                this.createdAt,
                this.expiresAt,
                this.position,
                this.enteredAt
        );
    }

    // 비즈니스 로직 - 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 비즈니스 로직 - 활성 상태 여부
    public boolean isActive() {
        return status == QueueStatus.ACTIVE && !isExpired();
    }

    // ID 할당 (Infrastructure 레이어에서만 사용)
    public void assignId(Long id) {
        this.id = id;
    }

    public void assignPosition(Integer position) {
        // position 업데이트 로직
    }

    // Getters
    public Long getId() { return id; }
    public String getTokenValue() { return tokenValue; }
    public Long getUserId() { return userId; }
    public Long getConcertId() { return concertId; }
    public QueueStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Integer getPosition() { return position; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
}

