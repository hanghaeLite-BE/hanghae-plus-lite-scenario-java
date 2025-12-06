package kr.hhplus.be.server.domain.model;

// TransactionType enum
public enum TransactionType {
    CHARGE,     // 충전
    DEDUCT,     // 차감 (결제, 환불 등)
    REFUND      // 환불
}
