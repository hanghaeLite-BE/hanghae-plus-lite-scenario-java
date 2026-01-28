package kr.hhplus.be.server.infrastructure.external;

import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 데이터 플랫폼 외부 API 호출 클라이언트 (Mock)
 * 
 * 이 클래스는 실시간 예약정보를 데이터 플랫폼에 전송하는 역할을 합니다.
 * 현재는 Mock 구현으로, 실제 HTTP 호출을 시뮬레이션합니다.
 * 
 * 문제점:
 * - 외부 호출이 동기적으로 이루어져 트랜잭션 내부에서 호출 가능
 * - 네트워크 지연이 전체 비즈니스 로직 응답시간에 영향
 * - 실패 시 정합성 문제 발생 가능
 */
@Component
public class DataPlatformClient {
    private static final Logger log = LoggerFactory.getLogger(DataPlatformClient.class);

    /**
     * 예약 확정 이벤트를 데이터 플랫폼에 전송
     * (동기 호출 - 트랜잭션 내부에서 직접 호출됨)
     * 
     * @param payload 전송할 예약 정보 페이로드
     */
    public void postReservationEvent(ReservationEventPayload payload) {
        try {
            // 외부 API 호출 시뮬레이션 (지연 추가)
            simulateNetworkDelay();
            
            // Mock API 호출 (실제로는 HTTP 요청)
            log.info("데이터 플랫폼에 예약정보 전송: reservationId={}, userId={}, concertId={}, seatId={}, paidAmount={}",
                    payload.getReservationId(), payload.getUserId(), payload.getConcertId(),
                    payload.getSeatId(), payload.getPaidAmount());
            
            // Mock 성공 응답
            if (payload.getPaidAmount() < 0) {
                throw new RuntimeException("Invalid payment amount");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("데이터 플랫폼에 전송 중 중단됨", e);
        } catch (Exception e) {
            // 문제점: 외부 에러가 트랜잭션을 롤백할 수 있음
            log.error("데이터 플랫폼 전송 실패", e);
            throw new RuntimeException("데이터 플랫폼 전송 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 네트워크 지연 시뮬레이션
     * (아쉬운 사례: 트랜잭션 내부에서 동기적으로 대기)
     */
    private void simulateNetworkDelay() throws InterruptedException {
        // 500ms의 외부 API 호출 지연을 시뮬레이션
        Thread.sleep(500);
    }

    /**
     * 예약 이벤트 페이로드
     * STEP9: Kafka 메시지 변환용 DTO
     */
    @Builder
    public static class ReservationEventPayload {
        private final String eventId;        // Kafka에서 전달받은 UUID (추적용)
        private final Long reservationId;
        private final Long userId;
        private final Long concertId;
        private final Long seatId;
        private final Long paidAmount;
        private final String occurredAt;    // ISO-8601 형식 문자열

        // Lombok @Builder가 생성하는 생성자
        public ReservationEventPayload(String eventId, Long reservationId, Long userId, Long concertId, 
                                     Long seatId, Long paidAmount, String occurredAt) {
            this.eventId = eventId;
            this.reservationId = reservationId;
            this.userId = userId;
            this.concertId = concertId;
            this.seatId = seatId;
            this.paidAmount = paidAmount;
            this.occurredAt = occurredAt;
        }

        public String getEventId() { return eventId; }
        public Long getReservationId() { return reservationId; }
        public Long getUserId() { return userId; }
        public Long getConcertId() { return concertId; }
        public Long getSeatId() { return seatId; }
        public Long getPaidAmount() { return paidAmount; }
        public String getOccurredAt() { return occurredAt; }
    }
}
