package kr.hhplus.be.server.infrastructure.reservation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 처리된 Kafka 이벤트 기록
 * 
 * STEP9 개선 사례: Consumer의 멱등성을 보장하기 위해
 * 이미 처리된 eventId를 DB에 저장하고, 중복 처리를 방지한다.
 * 
 * Kafka는 at-least-once 보장이므로 같은 메시지가 여러 번 전달될 수 있다:
 * - 네트워크 재시도
 * - Consumer 재시작
 * - Rebalancing 중 중복 전달
 * 
 * 이 테이블을 통해 "정확히 한 번" 처리를 구현한다.
 */
@Entity
@Table(name = "processed_events", uniqueConstraints = {
        @UniqueConstraint(columnNames = "event_id", name = "uk_processed_events_event_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEventEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Kafka 메시지의 eventId (UUID)
     * 유니크 제약: 같은 eventId는 1회만 저장 가능
     */
    @Column(nullable = false, length = 36)
    private String eventId;
    
    /**
     * 처리된 예약 ID
     */
    @Column(nullable = false)
    private Long reservationId;
    
    /**
     * 처리 결과 (SUCCESS, FAILED)
     */
    @Column(nullable = false, length = 20)
    private String status;  // SUCCESS or FAILED
    
    /**
     * 처리 시각
     */
    @Column(nullable = false)
    private LocalDateTime processedAt;
    
    /**
     * 실패 사유 (실패 시에만 기록)
     */
    @Column(columnDefinition = "TEXT")
    private String failureReason;
}
