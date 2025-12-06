package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;

public class ReservationToken {
    private Long id;
    private final String token;
    private final Long userId;
    private final TokenStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final Long waitingNumber;

    // 비즈니스 규칙을 포함한 생성자
    private ReservationToken(String token, Long userId, TokenStatus status,
                             LocalDateTime createdAt, LocalDateTime expiresAt, Long waitingNumber) {
        this.token = token;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.waitingNumber = waitingNumber;
    }

    // 팩토리 메서드 - 새 대기 토큰 생성
    public static ReservationToken createWaitingToken(String token, Long userId, Long waitingNumber) {
        LocalDateTime now = LocalDateTime.now();
        return new ReservationToken(
                token,
                userId,
                TokenStatus.WAITING,
                now,
                now.plusMinutes(30), // 30분 후 만료
                waitingNumber
        );
    }

    // 팩토리 메서드 - 활성 토큰으로 전환
    public static ReservationToken createActiveToken(String token, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return new ReservationToken(
                token,
                userId,
                TokenStatus.ACTIVE,
                now,
                now.plusMinutes(10), // 10분간 활성
                null
        );
    }

    // 비즈니스 로직 - 토큰 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 비즈니스 로직 - 활성화 가능 여부
    public boolean canBeActivated() {
        return status == TokenStatus.WAITING && !isExpired();
    }

    // ID 할당 (Infrastructure 레이어에서만 사용)
    void assignId(Long id) {
        this.id = id;
    }

    // Getters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public TokenStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Long getWaitingNumber() { return waitingNumber; }
}

