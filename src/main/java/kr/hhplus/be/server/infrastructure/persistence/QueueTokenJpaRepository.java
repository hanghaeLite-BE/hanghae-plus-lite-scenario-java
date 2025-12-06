package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.QueueStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.QueueTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueTokenJpaRepository extends JpaRepository<QueueTokenEntity, Long> {

    Optional<QueueTokenEntity> findByTokenValue(String tokenValue);

    Optional<QueueTokenEntity> findByUserIdAndConcertId(Long userId, Long concertId);

    List<QueueTokenEntity> findByConcertIdAndStatusOrderByCreatedAtAsc(Long concertId, QueueStatus status);

    List<QueueTokenEntity> findByConcertIdAndStatus(Long concertId, QueueStatus status);

    List<QueueTokenEntity> findByStatus(QueueStatus status);

    List<QueueTokenEntity> findByExpiresAtBeforeAndStatusIn(
            LocalDateTime expiresAt,
            List<QueueStatus> statuses
    );

    Long countByConcertIdAndStatus(Long concertId, QueueStatus status);

    void deleteByExpiresAtBeforeAndStatus(LocalDateTime beforeDate, QueueStatus status);

    // 대기 순번 조회
    @Query("""
        SELECT COUNT(q) + 1 
        FROM QueueTokenEntity q 
        WHERE q.concertId = :concertId 
        AND q.status = 'WAITING' 
        AND q.createdAt < (
            SELECT q2.createdAt 
            FROM QueueTokenEntity q2 
            WHERE q2.tokenValue = :tokenValue
        )
        """)
    Integer getWaitingPosition(@Param("concertId") Long concertId,
                               @Param("tokenValue") String tokenValue);

    // 활성화할 토큰 조회 (오래된 대기열부터)
    @Query(value = """
        SELECT * FROM queue_tokens 
        WHERE concert_id = :concertId 
        AND status = 'WAITING' 
        ORDER BY created_at ASC 
        LIMIT :limit
        """, nativeQuery = true)
    List<QueueTokenEntity> findTokensReadyToActivate(@Param("concertId") Long concertId,
                                                     @Param("limit") int limit);

    // 대기 순번 업데이트 (배치용)
    @Modifying
    @Query("""
        UPDATE QueueTokenEntity q 
        SET q.position = (
            SELECT COUNT(q2) 
            FROM QueueTokenEntity q2 
            WHERE q2.concertId = q.concertId 
            AND q2.status = 'WAITING' 
            AND q2.createdAt <= q.createdAt
        ) 
        WHERE q.concertId = :concertId 
        AND q.status = 'WAITING'
        """)
    void updateTokenPositions(@Param("concertId") Long concertId);
}