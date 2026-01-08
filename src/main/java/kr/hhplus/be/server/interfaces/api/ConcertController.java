package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.booking.BookingService;
import kr.hhplus.be.server.domain.booking.Reservation;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertService;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;
    private final BookingService bookingService;
    private final MemberService memberService;

    // 콘서트 및 좌석 조회 (엔티티를 그대로 반환하는 아쉬운 패턴)
    @GetMapping("/concerts/{concertId}")
    public ConcertResponse getConcert(@PathVariable Long concertId) {
        Concert concert = concertService.getConcert(concertId);
        List<Seat> seats = concertService.getSeats(concertId);
        return new ConcertResponse(concert, seats);
    }

    // 예약 + 결제 동시 수행 (책임 분리가 안 된 API)
    @PostMapping("/bookings/confirm")
    public Reservation confirmBooking(@RequestBody BookingRequest request) {
        return bookingService.confirmBooking(request.memberId(), request.seatId());
    }

    // 포인트 충전
    @PostMapping("/members/{memberId}/points/charge")
    public Member chargePoint(@PathVariable Long memberId, @RequestBody ChargeRequest request) {
        return memberService.chargePoint(memberId, request.amount());
    }

    public record ConcertResponse(Concert concert, List<Seat> seats) {}
    public record BookingRequest(Long memberId, Long seatId) {}
    public record ChargeRequest(Long amount) {}
}
