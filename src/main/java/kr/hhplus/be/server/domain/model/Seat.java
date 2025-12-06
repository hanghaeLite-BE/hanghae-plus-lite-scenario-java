package kr.hhplus.be.server.domain.model;

public class Seat {
    private final Long concertId;
    private final int seatNumber;
    private final SeatStatus status;
    private final Long reservedByUserId;

    public enum SeatStatus {
        AVAILABLE, RESERVED, SOLD
    }

    private Seat(Long concertId, int seatNumber, SeatStatus status, Long reservedByUserId) {
        this.concertId = concertId;
        this.seatNumber = seatNumber;
        this.status = status;
        this.reservedByUserId = reservedByUserId;
    }

    public static Seat available(Long concertId, int seatNumber) {
        return new Seat(concertId, seatNumber, SeatStatus.AVAILABLE, null);
    }

    // 비즈니스 규칙: 좌석 예약 가능 여부 확인
    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    // 비즈니스 규칙: 좌석 예약
    public Seat reserve(Long userId) {
        if (!isAvailable()) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        return new Seat(this.concertId, this.seatNumber, SeatStatus.RESERVED, userId);
    }

    // 비즈니스 규칙: 좌석 판매 완료
    public Seat markAsSold() {
        if (status != SeatStatus.RESERVED) {
            throw new IllegalStateException("예약된 좌석만 판매 완료 처리할 수 있습니다.");
        }
        return new Seat(this.concertId, this.seatNumber, SeatStatus.SOLD, this.reservedByUserId);
    }

    // Getters
    public Long getConcertId() { return concertId; }
    public int getSeatNumber() { return seatNumber; }
    public SeatStatus getStatus() { return status; }
    public Long getReservedByUserId() { return reservedByUserId; }
}
