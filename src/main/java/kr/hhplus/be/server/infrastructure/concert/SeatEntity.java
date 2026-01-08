package kr.hhplus.be.server.infrastructure.concert;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.concert.SeatStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long concertId;
    private Integer seatNo;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private Long price;
    private LocalDateTime reservedUntil;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }
    public Integer getSeatNo() { return seatNo; }
    public void setSeatNo(Integer seatNo) { this.seatNo = seatNo; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public LocalDateTime getReservedUntil() { return reservedUntil; }
    public void setReservedUntil(LocalDateTime reservedUntil) { this.reservedUntil = reservedUntil; }
}
