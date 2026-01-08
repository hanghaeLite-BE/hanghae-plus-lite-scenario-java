package kr.hhplus.be.server.infrastructure.concert;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.concert.SeatStatus;

@Entity
@Table(name = "seat")
public class SeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long concertId;
    private int seatNo;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private Long price;

    private java.time.LocalDateTime reservedUntil;

    public SeatEntity() {}

    public java.time.LocalDateTime getReservedUntil() { return reservedUntil; }
    public void setReservedUntil(java.time.LocalDateTime reservedUntil) { this.reservedUntil = reservedUntil; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }
    public int getSeatNo() { return seatNo; }
    public void setSeatNo(int seatNo) { this.seatNo = seatNo; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
}
