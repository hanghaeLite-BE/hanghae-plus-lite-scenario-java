package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.Reservation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
class ReservationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "concert_id")
    private Long concertId;

    @Column(name = "seat_number")
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    private Reservation.ReservationStatus status;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private Long price;

    // 기본 생성자, getters, setters
    protected ReservationEntity() {}

    public void setStatus(ReservationStatus reservationStatus) {

    }

    public enum ReservationStatus {
        RESERVED, EXPIRED, CONFIRMED, CANCELLED
    }



    // getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public Reservation.ReservationStatus getStatus() { return status; }
    public void setStatus(Reservation.ReservationStatus status) { this.status = status; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
}