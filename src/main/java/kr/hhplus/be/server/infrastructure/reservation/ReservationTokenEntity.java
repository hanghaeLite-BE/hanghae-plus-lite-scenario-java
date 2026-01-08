package kr.hhplus.be.server.infrastructure.reservation;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.reservation.TokenStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_tokens")
public class ReservationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private Long userId;
    private Long concertId;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }
    public TokenStatus getStatus() { return status; }
    public void setStatus(TokenStatus status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
