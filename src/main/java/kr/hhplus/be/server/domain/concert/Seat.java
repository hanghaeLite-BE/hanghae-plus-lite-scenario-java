package kr.hhplus.be.server.domain.concert;

public class Seat {
    private Long id;
    private Long concertId;
    private int seatNo;
    private SeatStatus status;
    private Long price;
    private java.time.LocalDateTime reservedUntil;

    public Seat() {}

    public Seat(Long id, Long concertId, int seatNo, SeatStatus status, Long price, java.time.LocalDateTime reservedUntil) {
        this.id = id;
        this.concertId = concertId;
        this.seatNo = seatNo;
        this.status = status;
        this.price = price;
        this.reservedUntil = reservedUntil;
    }

    public void reserve(long ttlMinutes) {
        if (!isAvailable()) {
            throw new IllegalStateException("좌석이 예약 가능한 상태가 아닙니다.");
        }
        this.status = SeatStatus.RESERVED;
        this.reservedUntil = java.time.LocalDateTime.now().plusMinutes(ttlMinutes);
    }

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE || 
               (this.status == SeatStatus.RESERVED && reservedUntil != null && reservedUntil.isBefore(java.time.LocalDateTime.now()));
    }

    public void confirm() {
        if (this.status != SeatStatus.RESERVED) {
            throw new IllegalStateException("예약된 좌석만 확정할 수 있습니다.");
        }
        this.status = SeatStatus.SOLD;
    }

    public Long getId() { return id; }
    public Long getConcertId() { return concertId; }
    public int getSeatNo() { return seatNo; }
    public SeatStatus getStatus() { return status; }
    public Long getPrice() { return price; }
    public java.time.LocalDateTime getReservedUntil() { return reservedUntil; }
}
