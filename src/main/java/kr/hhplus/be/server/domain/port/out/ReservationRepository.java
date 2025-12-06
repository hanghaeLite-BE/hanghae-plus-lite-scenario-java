package kr.hhplus.be.server.domain.port.out;

import kr.hhplus.be.server.domain.model.Reservation;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Reservation findById(Long id);
    Reservation findByUserIdAndConcertIdAndSeatNumber(Long userId, Long concertId, int seatNumber);
}