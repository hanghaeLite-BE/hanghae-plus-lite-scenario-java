package kr.hhplus.be.server.domain.booking;

import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatRepository;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final MemberRepository memberRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Reservation confirmBooking(Long memberId, Long seatId) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        // 2. 좌석 조회 및 상태 검증
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 좌석입니다."));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new RuntimeException("이미 예약되었거나 판매된 좌석입니다.");
        }

        // 3. 좌석 상태 변경 (영속성 엔티티 직접 수정)
        seat.setStatus(Seat.SeatStatus.SOLD);
        seatRepository.save(seat);

        // 4. 예약 생성
        Reservation reservation = new Reservation(memberId, seatId);
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        Reservation savedReservation = reservationRepository.save(reservation);

        // 5. 포인트 차감
        if (member.getPoint() < seat.getPrice()) {
            throw new RuntimeException("포인트가 부족합니다.");
        }
        member.setPoint(member.getPoint() - seat.getPrice());
        memberRepository.save(member);

        // 6. 결제 내역 생성
        Payment payment = new Payment(savedReservation.getId(), seat.getPrice());
        paymentRepository.save(payment);

        return savedReservation;
    }
}
