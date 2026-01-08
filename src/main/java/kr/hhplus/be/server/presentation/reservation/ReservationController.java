package kr.hhplus.be.server.presentation.reservation;

import kr.hhplus.be.server.application.reservation.ConfirmReservationUseCase;
import kr.hhplus.be.server.application.reservation.ReserveSeatUseCase;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReserveSeatUseCase reserveSeatUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;

    public ReservationController(ReserveSeatUseCase reserveSeatUseCase,
                                 ConfirmReservationUseCase confirmReservationUseCase) {
        this.reserveSeatUseCase = reserveSeatUseCase;
        this.confirmReservationUseCase = confirmReservationUseCase;
    }

    @PostMapping
    public Reservation reserve(@RequestBody ReservationRequest request) {
        return reserveSeatUseCase.reserve(new ReserveSeatUseCase.Command(
                request.getMemberId(), request.getSeatId(), request.getToken()
        ));
    }

    @PostMapping("/{reservationId}/confirm")
    public void confirm(@PathVariable Long reservationId, @RequestBody ReservationRequest request) {
        confirmReservationUseCase.confirm(new ConfirmReservationUseCase.Command(
                reservationId, request.getMemberId()
        ));
    }

    public static class ReservationRequest {
        private Long memberId;
        private Long seatId;
        private String token;

        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public Long getSeatId() { return seatId; }
        public void setSeatId(Long seatId) { this.seatId = seatId; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
