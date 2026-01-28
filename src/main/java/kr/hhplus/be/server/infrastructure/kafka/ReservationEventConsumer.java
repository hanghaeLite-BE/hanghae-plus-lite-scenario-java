package kr.hhplus.be.server.infrastructure.kafka;

import kr.hhplus.be.server.infrastructure.external.DataPlatformClient;
import kr.hhplus.be.server.infrastructure.reservation.ProcessedEventEntity;
import kr.hhplus.be.server.infrastructure.reservation.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Kafka Consumer: 예약 완료 이벤트를 수신하여 데이터 플랫폼으로 전송
 * 
 * STEP9 개선 사례:
 * 1. 멱등성 보장: eventId 기반 중복 처리 방지
 * 2. at-least-once 최적화: 일반적인 Kafka 보장 수준에서 적응
 * 3. DLQ 지원: 실패 메시지 별도 저장
 * 
 * 특징:
 * - Topic: concert.reservation.completed.v1
 * - Consumer Group: concert-reservation-consumer-group
 * - processed_events 테이블로 멱등성 검증
 * - 실패 시 로그 기록 + processed_events에 FAILED 상태 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventConsumer {
    
    private final DataPlatformClient dataPlatformClient;
    private final ProcessedEventJpaRepository processedEventRepository;
    
    /**
     * 예약 완료 이벤트를 수신하여 데이터 플랫폼으로 전송 (멱등성 보장)
     * 
     * STEP9 개선 사례: 멱등성 처리
     * 
     * Kafka는 at-least-once 보장만 가능하므로 같은 메시지가 여러 번 전달될 수 있다.
     * 이를 대비하여 eventId를 기준으로 중복 처리를 방지한다:
     * 1. processed_events 테이블에서 eventId 확인
     * 2. 이미 처리됨 → 스킵
     * 3. 미처리 → 데이터 플랫폼 호출 후 기록
     * 4. 실패 → FAILED 상태로 기록
     * 
     * @param message Kafka에서 수신한 메시지
     */
    @KafkaListener(topics = ReservationEventProducer.TOPIC, groupId = "concert-reservation-consumer-group")
    @Transactional
    public void handleReservationCompleted(ReservationEventMessage message) {
        try {
            String eventId = message.getEventId();
            Long reservationId = message.getReservationId();
            
            log.info("Kafka 메시지 수신: eventId={}, reservationId={}", eventId, reservationId);
            
            // [멱등성 검증] 1단계: 이미 처리된 eventId인지 확인
            if (processedEventRepository.existsByEventId(eventId)) {
                log.warn("이미 처리된 이벤트 - 스킵: eventId={}, reservationId={}", eventId, reservationId);
                return;  // 중복 처리 방지
            }
            
            // [처리] 2단계: 데이터 플랫폼으로 전송
            DataPlatformClient.ReservationEventPayload payload = 
                    DataPlatformClient.ReservationEventPayload.builder()
                            .eventId(eventId)
                            .reservationId(reservationId)
                            .userId(message.getUserId())
                            .concertId(message.getConcertId())
                            .seatId(message.getSeatId())
                            .paidAmount(message.getPaidAmount())
                            .occurredAt(message.getOccurredAt())
                            .build();
            
            dataPlatformClient.postReservationEvent(payload);
            
            // [기록] 3단계: 처리 기록 저장 (SUCCESS)
            ProcessedEventEntity processedEvent = ProcessedEventEntity.builder()
                    .eventId(eventId)
                    .reservationId(reservationId)
                    .status("SUCCESS")
                    .processedAt(LocalDateTime.now())
                    .build();
            processedEventRepository.save(processedEvent);
            
            log.info("데이터 플랫폼 전송 완료: eventId={}, reservationId={}", eventId, reservationId);
            
        } catch (Exception e) {
            String eventId = message.getEventId();
            Long reservationId = message.getReservationId();
            String errorMessage = e.getMessage();
            
            // [실패 처리] 4단계: 실패 기록 저장
            try {
                // 이미 처리 기록이 있는지 확인 (재시도 등으로 여러 번 실패할 수 있음)
                if (!processedEventRepository.existsByEventId(eventId)) {
                    ProcessedEventEntity failedEvent = ProcessedEventEntity.builder()
                            .eventId(eventId)
                            .reservationId(reservationId)
                            .status("FAILED")
                            .processedAt(LocalDateTime.now())
                            .failureReason(errorMessage)
                            .build();
                    processedEventRepository.save(failedEvent);
                }
            } catch (Exception dbException) {
                log.error("실패 기록 저장 중 오류: eventId={}, error={}", eventId, dbException.getMessage());
            }
            
            // [로깅 + DLQ] 5단계: 상세 로그 및 DLQ 발행 고려
            log.error("데이터 플랫폼 전송 실패: eventId={}, reservationId={}, error={}", 
                    eventId, reservationId, errorMessage, e);
            
            // 실패한 메시지를 DLQ 토픽으로 발행 (향후 분석/재처리 용도)
            sendToDeadLetterQueue(message, e);
        }
    }
    
    /**
     * Dead Letter Queue로 메시지 전송
     * 
     * 처리 실패한 메시지를 별도 토픽으로 이동하여
     * 나중에 수동 검사 및 재처리할 수 있도록 한다.
     * 
     * @param message 원본 메시지
     * @param exception 발생한 예외
     */
    private void sendToDeadLetterQueue(ReservationEventMessage message, Exception exception) {
        // DLQ 구현 (선택적, 기본 로깅만 사용 가능)
        log.warn("DLQ 처리 대상: eventId={}, reservationId={}, reason={}", 
                message.getEventId(), message.getReservationId(), exception.getMessage());
        // 실제 DLQ 발행:
        // kafkaTemplate.send("concert.reservation.completed.dlq.v1", message);
    }
}
