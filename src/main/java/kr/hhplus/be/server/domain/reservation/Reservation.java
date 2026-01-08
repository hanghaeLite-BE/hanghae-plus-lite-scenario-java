package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private Long memberId;
    private Long seatId;
    private ReservationStatus status;
    private LocalDateTime reservedUntil;
    private LocalDateTime createdAt;

    public Reservation() {}

    public Reservation(Long id, Long memberId, Long seatId, ReservationStatus status, LocalDateTime reservedUntil, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.seatId = seatId;
        this.status = status;
        this.reservedUntil = reservedUntil;
        this.createdAt = createdAt;
    }

    public static Reservation create(Long memberId, Long seatId) {
        return new Reservation(null, memberId, seatId, ReservationStatus.PENDING, LocalDateTime.now().plusMinutes(5), LocalDateTime.now());
    }

    public void confirm() {
        // 아쉬운 점: 만료 확인 로직에서 LocalDateTime.now()를 직접 호출하여 테스트 코드 작성이 어려움
        if (this.reservedUntil.isBefore(java.time.LocalDateTime.now())) {
            this.status = ReservationStatus.CANCELLED;
            throw new RuntimeException("expired");
        }
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 예약만 확정할 수 있습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getSeatId() { return seatId; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
