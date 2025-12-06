package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.QueueToken;
import kr.hhplus.be.server.domain.model.QueueStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueTokenRepository {
    QueueToken save(QueueToken token);
    Optional<QueueToken> findById(Long id);
    Optional<QueueToken> findByTokenValue(String tokenValue);
    Optional<QueueToken> findByUserIdAndConcertId(Long userId, Long concertId);

    // 대기열 관리
    List<QueueToken> findWaitingTokensByConcertId(Long concertId);
    List<QueueToken> findActiveTokensByConcertId(Long concertId);
    Integer getWaitingPosition(Long concertId, String tokenValue);

    // 상태별 조회
    List<QueueToken> findByStatus(QueueStatus status);
    List<QueueToken> findExpiredTokens(LocalDateTime currentTime);

    // 토큰 활성화 관리
    List<QueueToken> findTokensReadyToActivate(Long concertId, int limit);
    Long countActiveTokensByConcertId(Long concertId);
    Long countWaitingTokensByConcertId(Long concertId);

    // 배치 처리용
    void deleteExpiredTokens(LocalDateTime beforeDate);
    void updateTokenPositions(Long concertId);
}