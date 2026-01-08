package kr.hhplus.be.server.application.reservation;

public interface ConfirmReservationUseCase {
    void confirm(Command command);

    record Command(
            Long reservationId,
            Long userId
    ) {}
}
