package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.SeatReservation;
import kr.hhplus.be.server.domain.model.SeatStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatReservationRepository {
    SeatReservation save(SeatReservation reservation);
    Optional<SeatReservation> findById(Long id);
    Optional<SeatReservation> findByConcertIdAndSeatNumber(Long concertId, Integer seatNumber);

    // 동시성 제어를 위한 비관적 락
    Optional<SeatReservation> findByConcertIdAndSeatNumberWithLock(Long concertId, Integer seatNumber);

    // 좌석 상태별 조회
    List<SeatReservation> findByConcertIdAndStatus(Long concertId, SeatStatus status);
    List<SeatReservation> findAvailableSeats(Long concertId);

    // 만료된 예약 처리
    List<SeatReservation> findExpiredReservations(LocalDateTime now);
    void releaseExpiredReservations(LocalDateTime now);

    // 특정 사용자의 예약 조회
    List<SeatReservation> findByUserIdAndStatus(Long userId, SeatStatus status);
}
