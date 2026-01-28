package kr.hhplus.be.server.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Kafka Producer: 예약 완료 이벤트 발행
 * 
 * STEP9 개선 사례: 커밋 이후 발행 보장
 * 
 * TransactionSynchronizationManager.registerSynchronization()을 활용하여
 * 트랜잭션이 성공적으로 커밋된 이후에만 Kafka 메시지를 발행한다.
 * 
 * 이를 통해:
 * 1. 롤백 시 메시지 발행 방지 (정합성 보장)
 * 2. 메시지 발행 실패가 도메인 트랜잭션에 영향 없음
 * 3. 명시적인 코드 구조로 commit-after-send 보장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventProducer {
    
    // 토픽명: "v1" 버전 포함 (향후 스키마 변경 시 v2로 확장 가능)
    public static final String TOPIC = "concert.reservation.completed.v1";
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * 예약 완료 이벤트를 Kafka로 발행 (커밋 이후 보장)
     * 
     * STEP9 개선 사례: TransactionSynchronizationManager를 사용하여
     * 현재 트랜잭션이 성공적으로 커밋된 이후에만 메시지를 발행한다.
     * 
     * @param message 예약 이벤트 메시지
     */
    public void publishReservationCompletedAfterCommit(ReservationEventMessage message) {
        try {
            log.info("Kafka 메시지 발행 등록 (커밋 이후): eventId={}, topic={}", 
                    message.getEventId(), TOPIC);
            
            // TransactionSynchronization을 등록하여 커밋 후 처리
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                String key = message.getReservationId().toString();
                                kafkaTemplate.send(TOPIC, key, message);
                                
                                log.info("Kafka 메시지 발행 완료 (커밋 이후): eventId={}, reservationId={}", 
                                        message.getEventId(), message.getReservationId());
                            } catch (Exception e) {
                                log.error("Kafka 메시지 발행 실패 (커밋 이후): eventId={}, reservationId={}, error={}", 
                                        message.getEventId(), message.getReservationId(), 
                                        e.getMessage(), e);
                                // afterCommit에서 예외 발생은 도메인 트랜잭션에 영향 없음
                            }
                        }
                        
                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_ROLLED_BACK) {
                                log.warn("트랜잭션 롤백 - Kafka 메시지 발행 취소: eventId={}, reservationId={}", 
                                        message.getEventId(), message.getReservationId());
                            }
                        }
                    }
            );
            
            log.debug("Kafka 메시지 발행 콜백 등록 완료: eventId={}", message.getEventId());
        } catch (Exception e) {
            log.error("Kafka 메시지 발행 콜백 등록 실패: eventId={}", message.getEventId(), e);
            throw e;
        }
    }
    
    /**
     * [구버전] 예약 완료 이벤트를 Kafka로 발행 (호환성 유지)
     * 
     * @deprecated publishReservationCompletedAfterCommit() 사용 권장
     * @param message 예약 이벤트 메시지
     */
    @Deprecated
    public void publishReservationCompleted(ReservationEventMessage message) {
        try {
            log.info("Kafka 메시지 발행 시작: eventId={}, topic={}", message.getEventId(), TOPIC);
            
            // Key: reservationId로 같은 예약의 메시지는 같은 partition으로 발행
            String key = message.getReservationId().toString();
            
            kafkaTemplate.send(TOPIC, key, message);
            
            log.info("Kafka 메시지 발행 완료: eventId={}, reservationId={}", 
                    message.getEventId(), message.getReservationId());
        } catch (Exception e) {
            log.error("Kafka 메시지 발행 실패: eventId={}, error={}", 
                    message.getEventId(), e.getMessage(), e);
            throw e;
        }
    }
}
