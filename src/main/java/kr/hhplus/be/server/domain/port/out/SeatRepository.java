package kr.hhplus.be.server.domain.port.out;

import kr.hhplus.be.server.domain.model.Seat;

public interface SeatRepository {
    Seat findByConcertIdAndSeatNumber(Long concertId, int seatNumber);
    Seat save(Seat seat);
    boolean existsByConcertIdAndSeatNumberAndStatus(Long concertId, int seatNumber, Seat.SeatStatus status);
}

