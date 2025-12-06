package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.entity.SeatReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatReservationJpaRepository extends JpaRepository<SeatReservationEntity, Long> {

    Optional<SeatReservationEntity> findByConcertIdAndSeatNumber(Long concertId, Integer seatNumber);

    // 비관적 락으로 좌석 조회 (동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatReservationEntity s WHERE s.concertId = :concertId AND s.seatNumber = :seatNumber")
    Optional<SeatReservationEntity> findByConcertIdAndSeatNumberWithLock(@Param("concertId") Long concertId,
                                                                         @Param("seatNumber") Integer seatNumber);

    // 콘서트별 상태별 좌석 조회
    List<SeatReservationEntity> findByConcertIdAndStatus(Long concertId, SeatStatus status);

    // 예약 가능한 좌석 조회 (AVAILABLE + 만료된 RESERVED)
    @Query("SELECT s FROM SeatReservationEntity s WHERE s.concertId = :concertId AND " +
            "(s.status = 'AVAILABLE' OR (s.status = 'RESERVED' AND s.expiresAt < :now))")
    List<SeatReservationEntity> findAvailableSeats(@Param("concertId") Long concertId,
                                                   @Param("now") LocalDateTime now);

    // 만료된 예약 조회
    @Query("SELECT s FROM SeatReservationEntity s WHERE s.status = 'RESERVED' AND s.expiresAt < :now")
    List<SeatReservationEntity> findExpiredReservations(@Param("now") LocalDateTime now);

    // 만료된 예약 일괄 해제
    @Modifying
    @Query("UPDATE SeatReservationEntity s SET s.status = 'AVAILABLE', s.userId = null, " +
            "s.reservedAt = null, s.expiresAt = null, s.updatedAt = :now " +
            "WHERE s.status = 'RESERVED' AND s.expiresAt < :now")
    void releaseExpiredReservations(@Param("now") LocalDateTime now);

    // 사용자별 예약 조회
    List<SeatReservationEntity> findByUserIdAndStatus(Long userId, SeatStatus status);

    // 콘서트별 통계 (성능 최적화를 위한 커버링 인덱스 활용)
    @Query("SELECT s.status, COUNT(s) FROM SeatReservationEntity s WHERE s.concertId = :concertId GROUP BY s.status")
    List<Object[]> countSeatsByStatus(@Param("concertId") Long concertId);
}
