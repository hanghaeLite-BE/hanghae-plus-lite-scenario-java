package kr.hhplus.be.server.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka Producer: 예약 완료 이벤트 발행
 * 
 * STEP9 기초 수준 구현
 * - Callback 없음 (fire-and-forget)
 * - 단순 메시지 발행만 수행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventProducer {
    
    public static final String TOPIC = "concert.reservation.completed";
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * 예약 완료 이벤트를 Kafka로 발행
     * 
     * @param message 예약 이벤트 메시지
     */
    public void publishReservationCompleted(ReservationEventMessage message) {
        try {
            log.info("Kafka 메시지 발행 시작: eventId={}, topic={}", message.getEventId(), TOPIC);
            
            // Key: reservationId로 같은 예약의 메시지는 같은 partition으로 발행
            String key = message.getReservationId().toString();
            
            kafkaTemplate.send(TOPIC, key, message);
            
            log.info("Kafka 메시지 발행 완료: eventId={}, reservationId={}", 
                    message.getEventId(), message.getReservationId());
        } catch (Exception e) {
            // 기초 수준: 발행 실패도 로그만 남김
            // (개선 사례에서는 이벤트 저장, 재시도 로직 추가)
            log.error("Kafka 메시지 발행 실패: eventId={}, error={}", 
                    message.getEventId(), e.getMessage(), e);
            throw e;
        }
    }
}
