package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.ReservationToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ReservationTokenRepository {

    @Transactional
    ReservationToken save(ReservationToken token);

    Optional<ReservationToken> findByToken(String token);
    Optional<ReservationToken> findByUserId(Long userId);
    void deleteByToken(String token);

    // 대기열 관리
    long countActiveTokens();
    long getWaitingPosition(String token);
    void activateNextTokens(int count);

    // 만료된 토큰 정리
    void deleteExpiredTokens();
}
