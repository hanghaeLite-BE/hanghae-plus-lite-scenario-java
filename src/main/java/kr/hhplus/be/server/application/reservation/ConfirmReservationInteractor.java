package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmReservationInteractor implements ConfirmReservationUseCase {

    private final ReservationRepositoryPort reservationRepository;
    private final MemberRepositoryPort memberRepository;
    private final SeatRepositoryPort seatRepository;

    public ConfirmReservationInteractor(
            ReservationRepositoryPort reservationRepository,
            MemberRepositoryPort memberRepository,
            SeatRepositoryPort seatRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    @Transactional
    public void execute(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!reservation.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 확정할 수 있습니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // Domain logic: Payment (point deduction)
        member.usePoints(seat.getPrice());
        memberRepository.save(member);

        // Domain logic: Update statuses
        reservation.confirm();
        seat.confirm();

        reservationRepository.save(reservation);
        seatRepository.save(seat);
    }
}
