package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.reservation.Reservation;

public interface ReserveSeatUseCase {
    Reservation reserve(Command command);

    record Command(Long userId, Long seatId, String token) {}
}
