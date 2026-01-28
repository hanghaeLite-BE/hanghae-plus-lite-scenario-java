package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.reservation.ReservationCompletedEvent;
import kr.hhplus.be.server.infrastructure.external.DataPlatformClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

/**
 * 예약 확정 이벤트 리스너
 * 
 * @TransactionalEventListener(phase = AFTER_COMMIT)로 표시하여
 * 트랜잭션 커밋 이후에만 실행된다.
 * 
 * 역할:
 * 1. 데이터 플랫폼에 예약 정보 전송
 * 2. 외부 API 호출 실패가 도메인 로직에 영향을 주지 않음
 * 3. 실패를 로그로 남겨 추적 가능하게 함 (eventId 기반)
 * 
 * 이를 통해:
 * - 트랜잭션과 외부 호출의 완벽한 분리
 * - 관심사의 분리 (도메인 로직 ↔ 데이터 연동 로직)
 * - 외부 시스템 장애가 핵심 비즈니스 영향을 주지 않음
 */
@Component
public class ReservationCompletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(ReservationCompletedEventListener.class);

    private final DataPlatformClient dataPlatformClient;

    public ReservationCompletedEventListener(DataPlatformClient dataPlatformClient) {
        this.dataPlatformClient = dataPlatformClient;
    }

    /**
     * 예약 확정 이벤트 처리
     * 
     * @TransactionalEventListener(phase = AFTER_COMMIT):
     * - 트랜잭션이 성공적으로 커밋된 후에만 호출됨
     * - 롤백된 경우 이 메서드는 호출되지 않음
     * 
     * @param event 예약 확정 이벤트
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleReservationCompleted(ReservationCompletedEvent event) {
        try {
            log.info("예약 확정 이벤트 처리 시작: eventId={}, reservationId={}", 
                    event.getEventId(), event.getReservationId());

            // 데이터 플랫폼 전송용 페이로드 구성
            // STEP8에서 STEP9로의 호환성 유지: Application Event에서 Kafka 메시지로 변환
            DataPlatformClient.ReservationEventPayload payload = 
                    DataPlatformClient.ReservationEventPayload.builder()
                            .eventId(event.getEventId())
                            .reservationId(event.getReservationId())
                            .userId(event.getUserId())
                            .concertId(event.getConcertId())
                            .seatId(event.getSeatId())
                            .paidAmount(event.getPaidAmount())
                            .occurredAt(event.getOccurredAt().toString())
                            .build();

            // 데이터 플랫폼으로 전송
            dataPlatformClient.postReservationEvent(payload);

            log.info("예약 확정 이벤트 처리 완료: eventId={}, reservationId={}", 
                    event.getEventId(), event.getReservationId());

        } catch (Exception e) {
            // 외부 API 호출 실패 시:
            // - 도메인 트랜잭션은 이미 커밋됨 (롤백되지 않음)
            // - 실패는 로그로 남겨짐 (eventId로 추적 가능)
            // - 다음 단계(STEP9)에서 재시도 메커니즘을 추가할 수 있음
            log.error("예약 확정 이벤트 처리 실패: eventId={}, reservationId={}, error={}",
                    event.getEventId(), event.getReservationId(), e.getMessage(), e);
        }
    }
}
