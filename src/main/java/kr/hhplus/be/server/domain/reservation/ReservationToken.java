package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public class ReservationToken {
    private final Long id;
    private final String token;
    private final Long userId;
    private final Long concertId;
    private TokenStatus status;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    public ReservationToken(Long id, String token, Long userId, Long concertId, TokenStatus status, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.concertId = concertId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static ReservationTokenBuilder builder() {
        return new ReservationTokenBuilder();
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public Long getConcertId() { return concertId; }
    public TokenStatus getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isValid() {
        return status == TokenStatus.ACTIVE && expiresAt.isAfter(LocalDateTime.now());
    }

    public void use() {
        this.status = TokenStatus.USED;
    }

    public static class ReservationTokenBuilder {
        private Long id;
        private String token;
        private Long userId;
        private Long concertId;
        private TokenStatus status;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;

        public ReservationTokenBuilder id(Long id) { this.id = id; return this; }
        public ReservationTokenBuilder token(String token) { this.token = token; return this; }
        public ReservationTokenBuilder userId(Long userId) { this.userId = userId; return this; }
        public ReservationTokenBuilder concertId(Long concertId) { this.concertId = concertId; return this; }
        public ReservationTokenBuilder status(TokenStatus status) { this.status = status; return this; }
        public ReservationTokenBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public ReservationTokenBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ReservationToken build() {
            return new ReservationToken(id, token, userId, concertId, status, expiresAt, createdAt);
        }
    }
}
