package kr.hhplus.be.server.infrastructure.persistence;

import java.time.LocalDateTime;

public interface ReservationTokenJpaRepository extends JpaRepository<ReservationTokenEntity, Long> {

    Optional<ReservationTokenEntity> findByToken(String token);

    Optional<ReservationTokenEntity> findByUserIdAndStatusIn(Long userId, List<TokenStatus> statuses);

    // 활성 토큰 수 조회 (좌석 예약 가능한 토큰)
    @Query("SELECT COUNT(t) FROM ReservationTokenEntity t WHERE t.status = 'ACTIVE' AND t.expiresAt > :now")
    long countActiveTokens(@Param("now") LocalDateTime now);

    // 대기 순번 조회
    @Query("SELECT COUNT(t) FROM ReservationTokenEntity t " +
            "WHERE t.status = 'WAITING' AND t.waitingNumber < :waitingNumber")
    long countWaitingTokensBeforeNumber(@Param("waitingNumber") Long waitingNumber);

    // 활성화 가능한 대기 토큰 조회 (생성 순서대로)
    @Query("SELECT t FROM ReservationTokenEntity t " +
            "WHERE t.status = 'WAITING' AND t.expiresAt > :now " +
            "ORDER BY t.createdAt ASC")
    List<ReservationTokenEntity> findWaitingTokensOrderByCreatedAt(@Param("now") LocalDateTime now);

    // 만료된 토큰 삭제
    @Modifying
    @Query("DELETE FROM ReservationTokenEntity t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    // 대기 번호의 최댓값 조회
    @Query("SELECT COALESCE(MAX(t.waitingNumber), 0) FROM ReservationTokenEntity t WHERE t.status = 'WAITING'")
    Long findMaxWaitingNumber();

    long countActiveTokens(LocalDateTime now);
}
