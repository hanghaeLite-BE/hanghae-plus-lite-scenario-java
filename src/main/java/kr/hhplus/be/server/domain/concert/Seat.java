package kr.hhplus.be.server.domain.concert;

import java.time.LocalDateTime;

public class Seat {
    private final Long id;
    private final Long concertId;
    private final Integer seatNo;
    private SeatStatus status;
    private final Long price;
    private LocalDateTime reservedUntil;

    public Seat(Long id, Long concertId, Integer seatNo, SeatStatus status, Long price, LocalDateTime reservedUntil) {
        this.id = id;
        this.concertId = concertId;
        this.seatNo = seatNo;
        this.status = status;
        this.price = price;
        this.reservedUntil = reservedUntil;
    }

    public static SeatBuilder builder() {
        return new SeatBuilder();
    }

    public Long getId() { return id; }
    public Long getConcertId() { return concertId; }
    public Integer getSeatNo() { return seatNo; }
    public SeatStatus getStatus() { return status; }
    public Long getPrice() { return price; }
    public LocalDateTime getReservedUntil() { return reservedUntil; }

    public void confirm() {
        this.status = SeatStatus.SOLD;
    }

    public static class SeatBuilder {
        private Long id;
        private Long concertId;
        private Integer seatNo;
        private SeatStatus status;
        private Long price;
        private LocalDateTime reservedUntil;

        public SeatBuilder id(Long id) { this.id = id; return this; }
        public SeatBuilder concertId(Long concertId) { this.concertId = concertId; return this; }
        public SeatBuilder seatNo(Integer seatNo) { this.seatNo = seatNo; return this; }
        public SeatBuilder status(SeatStatus status) { this.status = status; return this; }
        public SeatBuilder price(Long price) { this.price = price; return this; }
        public SeatBuilder reservedUntil(LocalDateTime reservedUntil) { this.reservedUntil = reservedUntil; return this; }

        public Seat build() {
            return new Seat(id, concertId, seatNo, status, price, reservedUntil);
        }
    }
}
