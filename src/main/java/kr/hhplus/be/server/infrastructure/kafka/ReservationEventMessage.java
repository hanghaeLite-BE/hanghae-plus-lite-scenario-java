package kr.hhplus.be.server.infrastructure.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kafka 메시지: 예약 완료 이벤트
 * Topic: concert.reservation.completed
 * 
 * STEP9 기초 수준 구현 - 단순 JSON 직렬화
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEventMessage {
    
    /**
     * 이벤트 고유 ID (UUID) - 추적용
     */
    private String eventId;
    
    /**
     * 예약 ID
     */
    private Long reservationId;
    
    /**
     * 사용자 ID
     */
    private Long userId;
    
    /**
     * 콘서트 ID
     */
    private Long concertId;
    
    /**
     * 좌석 ID
     */
    private Long seatId;
    
    /**
     * 결제 금액
     */
    private Long paidAmount;
    
    /**
     * 이벤트 발생 시각 (ISO-8601 형식)
     */
    private String occurredAt;
}
