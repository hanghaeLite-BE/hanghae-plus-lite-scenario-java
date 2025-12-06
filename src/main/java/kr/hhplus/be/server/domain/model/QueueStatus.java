package kr.hhplus.be.server.domain.model;

public enum QueueStatus {
    WAITING,    // 대기 중
    ACTIVE,     // 활성 (예약 가능)
    COMPLETED,  // 완료됨
    EXPIRED     // 만료됨
}
