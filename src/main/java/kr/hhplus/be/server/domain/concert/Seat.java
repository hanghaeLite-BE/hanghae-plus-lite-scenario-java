package kr.hhplus.be.server.domain.concert;

public class Seat {
    private Long id;
    private Long concertId;
    private int seatNo;
    private SeatStatus status;
    private Long price;

    public Seat() {}

    public Seat(Long id, Long concertId, int seatNo, SeatStatus status, Long price) {
        this.id = id;
        this.concertId = concertId;
        this.seatNo = seatNo;
        this.status = status;
        this.price = price;
    }

    public void reserve() {
        // 아쉬운 점: 동시성 처리를 위한 낙관적/비관적 락 대신 단순 상태 변경만 수행
        // (이후 통합 테스트에서 경합 발생시 실패할 가능성이 높음)
        if (this.status != SeatStatus.AVAILABLE) {
            throw new RuntimeException("already reserved");
        }
        this.status = SeatStatus.UNAVAILABLE;
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
}
