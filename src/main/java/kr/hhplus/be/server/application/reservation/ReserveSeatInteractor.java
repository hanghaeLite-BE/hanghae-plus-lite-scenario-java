package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReserveSeatInteractor implements ReserveSeatUseCase {

    private final SeatRepositoryPort seatRepository;
    private final ReservationRepositoryPort reservationRepository;

    public ReserveSeatInteractor(SeatRepositoryPort seatRepository, ReservationRepositoryPort reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public Reservation execute(Long memberId, Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        seat.reserve(); // Domain logic: Seat status check and change
        seatRepository.save(seat);

        Reservation reservation = Reservation.create(memberId, seatId);
        return reservationRepository.save(reservation);
    }
}
