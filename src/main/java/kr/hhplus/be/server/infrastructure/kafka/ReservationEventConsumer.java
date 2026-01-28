package kr.hhplus.be.server.infrastructure.kafka;

import kr.hhplus.be.server.infrastructure.external.DataPlatformClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer: 예약 완료 이벤트를 수신하여 데이터 플랫폼으로 전송
 * 
 * STEP9 기초 수준 구현
 * - Topic: concert.reservation.completed
 * - Consumer Group: concert-reservation-consumer-group
 * - 실패 시 로그 기록만 수행 (재시도 로직 없음)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventConsumer {
    
    private final DataPlatformClient dataPlatformClient;
    
    /**
     * 예약 완료 이벤트를 수신하여 데이터 플랫폼으로 전송
     * 
     * @param message Kafka에서 수신한 메시지
     */
    @KafkaListener(topics = ReservationEventProducer.TOPIC, groupId = "concert-reservation-consumer-group")
    public void handleReservationCompleted(ReservationEventMessage message) {
        try {
            log.info("Kafka 메시지 수신: eventId={}, reservationId={}", 
                    message.getEventId(), message.getReservationId());
            
            // 데이터 플랫폼으로 전송
            DataPlatformClient.ReservationEventPayload payload = 
                    DataPlatformClient.ReservationEventPayload.builder()
                            .eventId(message.getEventId())
                            .reservationId(message.getReservationId())
                            .userId(message.getUserId())
                            .concertId(message.getConcertId())
                            .seatId(message.getSeatId())
                            .paidAmount(message.getPaidAmount())
                            .occurredAt(message.getOccurredAt())
                            .build();
            
            dataPlatformClient.postReservationEvent(payload);
            
            log.info("데이터 플랫폼 전송 완료: eventId={}, reservationId={}", 
                    message.getEventId(), message.getReservationId());
        } catch (Exception e) {
            // 기초 수준: 실패 로그만 기록
            // (개선 사례에서는 Dead Letter Queue, 재시도 로직 추가)
            log.error("데이터 플랫폼 전송 실패: eventId={}, reservationId={}, error={}", 
                    message.getEventId(), message.getReservationId(), e.getMessage(), e);
        }
    }
}
