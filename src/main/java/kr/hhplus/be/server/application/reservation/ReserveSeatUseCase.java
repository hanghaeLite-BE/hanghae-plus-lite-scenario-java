package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.reservation.Reservation;

public interface ReserveSeatUseCase {
    Reservation execute(Long memberId, Long seatId);
}
