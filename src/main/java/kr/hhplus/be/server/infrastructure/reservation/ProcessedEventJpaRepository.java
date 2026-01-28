package kr.hhplus.be.server.infrastructure.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ProcessedEvent 저장소
 * 
 * Kafka Consumer의 멱등성 검증을 위해 사용
 */
@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, Long> {
    
    /**
     * eventId로 처리 기록 조회
     * 
     * @param eventId 이벤트 ID (UUID)
     * @return 처리 기록 (있으면)
     */
    Optional<ProcessedEventEntity> findByEventId(String eventId);
    
    /**
     * eventId로 처리 여부 확인
     * 
     * @param eventId 이벤트 ID (UUID)
     * @return true if already processed
     */
    boolean existsByEventId(String eventId);
}
