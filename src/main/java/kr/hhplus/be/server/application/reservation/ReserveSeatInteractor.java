package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReserveSeatInteractor implements ReserveSeatUseCase {

    private final SeatRepositoryPort seatRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final ReservationTokenRepositoryPort tokenRepository;

    public ReserveSeatInteractor(SeatRepositoryPort seatRepository,
                                ReservationRepositoryPort reservationRepository,
                                ReservationTokenRepositoryPort tokenRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    @Transactional
    public Reservation reserve(Command command) {
        // 1. 토큰 검증
        ReservationToken token = tokenRepository.findByToken(command.token())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (!token.isValid()) {
            throw new IllegalStateException("만료되었거나 이미 사용된 토큰입니다.");
        }

        // 2. 좌석 선점 (DB 원자적 업데이트 활용)
        boolean success = seatRepository.reserveAtomically(
                command.seatId(),
                LocalDateTime.now().plusMinutes(5)
        );

        if (!success) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }

        // 3. 토큰 사용 처리
        token.use();
        tokenRepository.save(token);

        // 4. 예약 레코드 생성
        Reservation reservation = Reservation.create(command.userId(), command.seatId());
        return reservationRepository.save(reservation);
    }
}
