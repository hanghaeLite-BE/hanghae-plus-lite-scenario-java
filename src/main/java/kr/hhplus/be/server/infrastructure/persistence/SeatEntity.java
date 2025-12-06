package kr.hhplus.be.server.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Column(name = "reserved_by_user_id")
    private Long reservedByUserId;

    protected SeatEntity() {}

    public enum SeatStatus {
        AVAILABLE, RESERVED, SOLD
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public Long getReservedByUserId() { return reservedByUserId; }
    public void setReservedByUserId(Long reservedByUserId) { this.reservedByUserId = reservedByUserId; }
}
