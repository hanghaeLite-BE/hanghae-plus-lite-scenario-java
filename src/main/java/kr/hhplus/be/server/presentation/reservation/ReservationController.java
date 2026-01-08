package kr.hhplus.be.server.presentation.reservation;

import kr.hhplus.be.server.application.reservation.ConfirmReservationUseCase;
import kr.hhplus.be.server.application.reservation.ReserveSeatUseCase;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReserveSeatUseCase reserveSeatUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;

    public ReservationController(ReserveSeatUseCase reserveSeatUseCase, ConfirmReservationUseCase confirmReservationUseCase) {
        this.reserveSeatUseCase = reserveSeatUseCase;
        this.confirmReservationUseCase = confirmReservationUseCase;
    }

    @PostMapping
    public Reservation reserve(@RequestBody ReservationRequest request) {
        return reserveSeatUseCase.execute(request.getMemberId(), request.getSeatId());
    }

    @PostMapping("/{reservationId}/confirm")
    public void confirm(@PathVariable Long reservationId, @RequestBody ConfirmRequest request) {
        confirmReservationUseCase.execute(reservationId, request.getMemberId());
    }

    public static class ReservationRequest {
        private Long memberId;
        private Long seatId;
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public Long getSeatId() { return seatId; }
        public void setSeatId(Long seatId) { this.seatId = seatId; }
    }

    public static class ConfirmRequest {
        private Long memberId;
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
    }
}
