package kr.hhplus.be.server.domain.reservation;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 예약 확정 완료 이벤트
 * 
 * 트랜잭션 내부에서 발행되며, @TransactionalEventListener(phase = AFTER_COMMIT)으로
 * 트랜잭션 커밋 이후에만 처리된다.
 * 
 * 이를 통해:
 * 1. 트랜잭션과 외부 API 호출의 완벽한 분리
 * 2. 롤백 시 외부 호출이 발생하지 않음을 보장
 * 3. 외부 호출 실패가 도메인 로직에 영향을 주지 않음
 */
public class ReservationCompletedEvent extends ApplicationEvent {
    
    private final String eventId;
    private final Long reservationId;
    private final Long userId;
    private final Long concertId;
    private final Long seatId;
    private final Long paidAmount;
    private final LocalDateTime occurredAt;

    public ReservationCompletedEvent(Object source,
                                    Long reservationId,
                                    Long userId,
                                    Long concertId,
                                    Long seatId,
                                    Long paidAmount,
                                    LocalDateTime occurredAt) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
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
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
