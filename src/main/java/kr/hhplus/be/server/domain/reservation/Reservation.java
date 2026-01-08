package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public class Reservation {
    private final Long id;
    private final Long memberId;
    private final Long seatId;
    private ReservationStatus status;
    private final LocalDateTime createdAt;

    public Reservation(Long id, Long memberId, Long seatId, ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.seatId = seatId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Reservation create(Long memberId, Long seatId) {
        return new Reservation(null, memberId, seatId, ReservationStatus.PENDING, LocalDateTime.now());
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getSeatId() { return seatId; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }
}
