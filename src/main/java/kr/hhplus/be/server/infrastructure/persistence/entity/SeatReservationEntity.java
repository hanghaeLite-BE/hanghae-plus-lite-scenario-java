package kr.hhplus.be.server.infrastructure.persistence.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.model.SeatStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_reservations")
public class SeatReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SeatStatus status;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Long price;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA용 기본 생성자
    protected SeatReservationEntity() {}

    public SeatReservationEntity(Long concertId, Integer seatNumber, Long userId,
                                 SeatStatus status, LocalDateTime reservedAt,
                                 LocalDateTime expiresAt, Long price) {
        this.concertId = concertId;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.status = status;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Long getConcertId() { return concertId; }
    public Integer getSeatNumber() { return seatNumber; }
    public Long getUserId() { return userId; }
    public SeatStatus getStatus() { return status; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Long getPrice() { return price; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}

