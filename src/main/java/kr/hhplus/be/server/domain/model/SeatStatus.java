package kr.hhplus.be.server.domain.model;

// SeatStatus enum
public enum SeatStatus {
    AVAILABLE,  // 예약 가능
    RESERVED,   // 임시 예약 (5분간)
    SOLD        // 결제 완료 (확정)
}
