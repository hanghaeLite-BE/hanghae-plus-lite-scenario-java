package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;

public class SeatReservation {
    private Long id;
    private final Long concertId;
    private final Integer seatNumber;
    private final Long userId;
    private final SeatStatus status;
    private final LocalDateTime reservedAt;
    private final LocalDateTime expiresAt;
    private final Long price;

    private SeatReservation(Long concertId, Integer seatNumber, Long userId,
                            SeatStatus status, LocalDateTime reservedAt,
                            LocalDateTime expiresAt, Long price) {
        this.concertId = concertId;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.status = status;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.price = price;
    }

    // 팩토리 메서드 - 사용 가능한 좌석 생성
    public static SeatReservation createAvailableSeat(Long concertId, Integer seatNumber, Long price) {
        return new SeatReservation(
                concertId,
                seatNumber,
                null, // userId는 null
                SeatStatus.AVAILABLE,
                null, // reservedAt는 null
                null, // expiresAt는 null
                price
        );
    }

    // 팩토리 메서드 - 임시 예약 생성 (5분간)
    public static SeatReservation createTemporaryReservation(Long concertId, Integer seatNumber,
                                                             Long userId, Long price) {
        LocalDateTime now = LocalDateTime.now();
        return new SeatReservation(
                concertId,
                seatNumber,
                userId,
                SeatStatus.RESERVED,
                now,
                now.plusMinutes(5), // 5분 후 만료
                price
        );
    }

    // 팩토리 메서드 - 확정 예약 (결제 완료)
    public static SeatReservation createConfirmedReservation(Long concertId, Integer seatNumber,
                                                             Long userId, Long price) {
        LocalDateTime now = LocalDateTime.now();
        return new SeatReservation(
                concertId,
                seatNumber,
                userId,
                SeatStatus.SOLD,
                now,
                null, // 확정 예약은 만료 없음
                price
        );
    }

    // 비즈니스 로직 - 예약 만료 여부
    public boolean isExpired() {
        if (status != SeatStatus.RESERVED || expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 비즈니스 로직 - 예약 가능 여부
    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE || isExpired();
    }

    // 비즈니스 로직 - 특정 사용자가 예약 중인지 확인
    public boolean isReservedBy(Long userId) {
        return status == SeatStatus.RESERVED &&
                this.userId != null &&
                this.userId.equals(userId) &&
                !isExpired();
    }

    // 비즈니스 로직 - 결제 가능 여부
    public boolean canBeConfirmed(Long userId) {
        return isReservedBy(userId);
    }

    // ID 할당 (Infrastructure 레이어에서만 사용)
    public void assignId(Long id) {
        this.id = id;
    }

    // Getters
    public Long getId() { return id; }
    public Long getConcertId() { return concertId; }
    public Integer getSeatNumber() { return seatNumber; }
    public Long getUserId() { return userId; }
    public SeatStatus getStatus() { return status; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Long getPrice() { return price; }
}

