package kr.hhplus.be.server.presentation.reservation;

import kr.hhplus.be.server.application.reservation.ConfirmReservationDistributedLockFacade;
import kr.hhplus.be.server.application.reservation.ConfirmReservationUseCase;
import kr.hhplus.be.server.application.reservation.ReserveSeatDistributedLockFacade;
import kr.hhplus.be.server.application.reservation.ReserveSeatUseCase;
import kr.hhplus.be.server.domain.reservation.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReserveSeatUseCase reserveSeatUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;
    private final ReserveSeatDistributedLockFacade reserveSeatDistributedLockFacade;
    private final ConfirmReservationDistributedLockFacade confirmReservationDistributedLockFacade;

    @PostMapping
    public Reservation reserve(@RequestBody ReservationRequest request) {
        // STEP 6: 분산락 적용 버전 사용 (기존 interactor 직접 호출 대신 facade 호출)
        // 편의상 concertId를 1L로 고정하거나 request에 추가되었다고 가정.
        Long concertId = 1L; 
        return reserveSeatDistributedLockFacade.reserveSeat(
                concertId, 
                request.getSeatId(), 
                request.getMemberId(), 
                request.getToken()
        );
    }

    @PostMapping("/{reservationId}/confirm")
    public void confirm(@PathVariable Long reservationId, @RequestBody ReservationRequest request) {
        confirmReservationDistributedLockFacade.confirmReservation(reservationId, request.getMemberId());
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
