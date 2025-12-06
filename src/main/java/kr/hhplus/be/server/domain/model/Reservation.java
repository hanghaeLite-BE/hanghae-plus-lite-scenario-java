package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private final Long userId;
    private final Long concertId;
    private final int seatNumber;
    private final ReservationStatus status;
    private final LocalDateTime reservedAt;
    private final LocalDateTime expiresAt;
    private final Long price;

    public enum ReservationStatus {
        RESERVED, EXPIRED, CONFIRMED
    }

    private Reservation(Long userId, Long concertId, int seatNumber, Long price) {
        this.userId = userId;
        this.concertId = concertId;
        this.seatNumber = seatNumber;
        this.status = ReservationStatus.RESERVED;
        this.reservedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(5); // 5분 임시 예약
        this.price = price;
    }

    public static Reservation create(Long userId, Long concertId, int seatNumber, Long price) {
        return new Reservation(userId, concertId, seatNumber, price);
    }

    // 비즈니스 규칙: 예약이 만료되었는지 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt) && status == ReservationStatus.RESERVED;
    }

    // 비즈니스 규칙: 결제 완료로 상태 변경
    public Reservation confirmPayment() {
        if (isExpired()) {
            throw new IllegalStateException("예약이 만료되어 결제할 수 없습니다.");
        }
        return new Reservation(this.userId, this.concertId, this.seatNumber,
                ReservationStatus.CONFIRMED, this.reservedAt, this.expiresAt, this.price, this.id);
    }

    // 생성자 오버로드 (상태 변경용)
    private Reservation(Long userId, Long concertId, int seatNumber,
                        ReservationStatus status, LocalDateTime reservedAt,
                        LocalDateTime expiresAt, Long price, Long id) {
        this.userId = userId;
        this.concertId = concertId;
        this.seatNumber = seatNumber;
        this.status = status;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.price = price;
        this.id = id;
    }

    // ID 할당 (Infrastructure에서만 호출)
    void assignId(Long id) {
        this.id = id;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getConcertId() { return concertId; }
    public int getSeatNumber() { return seatNumber; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Long getPrice() { return price; }
}
