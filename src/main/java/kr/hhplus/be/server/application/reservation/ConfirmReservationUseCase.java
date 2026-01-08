package kr.hhplus.be.server.application.reservation;

public interface ConfirmReservationUseCase {
    void execute(Long reservationId, Long memberId);
}
