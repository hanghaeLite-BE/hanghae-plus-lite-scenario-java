package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.model.Reservation;
import kr.hhplus.be.server.domain.model.Seat;
import kr.hhplus.be.server.domain.port.in.ReserveSeatUseCase;
import kr.hhplus.be.server.domain.port.out.ReservationRepository;
import kr.hhplus.be.server.domain.port.out.SeatRepository;
import org.springframework.transaction.annotation.Transactional;

public class ReserveSeatService implements ReserveSeatUseCase {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public ReserveSeatService(ReservationRepository reservationRepository,
                              SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    @Transactional
    public Reservation reserve(ReserveSeatCommand command) {
        // 1. 좌석 조회 및 예약 가능 확인
        Seat seat = seatRepository.findByConcertIdAndSeatNumber(
                command.getConcertId(), command.getSeatNumber());

        if (!seat.isAvailable()) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }

        // 2. 좌석 상태 변경 (예약됨으로)
        Seat reservedSeat = seat.reserve(command.getUserId());
        seatRepository.save(reservedSeat);

        // 3. 예약 생성 (가격은 콘서트 정보에서 가져와야 하지만 여기서는 임시로 150000 설정)
        Reservation reservation = Reservation.create(
                command.getUserId(),
                command.getConcertId(),
                command.getSeatNumber(),
                150000L
        );

        // 4. 예약 저장
        return reservationRepository.save(reservation);
    }
}

