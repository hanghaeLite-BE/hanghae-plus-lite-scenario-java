package kr.hhplus.be.server.domain.model;

// TokenStatus enum
public enum TokenStatus {
    WAITING,    // 대기 중
    ACTIVE,     // 활성 (좌석 예약 가능)
    EXPIRED,    // 만료됨
    USED        // 사용 완료 (결제 완료)
}
