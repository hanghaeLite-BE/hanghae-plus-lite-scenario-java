package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private Long memberId;
    private Long seatId;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    public Reservation() {}

    public Reservation(Long id, Long memberId, Long seatId, ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.seatId = seatId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Reservation create(Long memberId, Long seatId) {
        return new Reservation(null, memberId, seatId, ReservationStatus.RESERVED, LocalDateTime.now());
    }

    public void confirm() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("이미 확정되었거나 확정할 수 없는 예약입니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getSeatId() { return seatId; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
