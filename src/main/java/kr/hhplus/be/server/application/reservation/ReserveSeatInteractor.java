package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.ConcertRankingService;
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
    private final ConcertRankingService concertRankingService;

    public ReserveSeatInteractor(SeatRepositoryPort seatRepository,
                                ReservationRepositoryPort reservationRepository,
                                ReservationTokenRepositoryPort tokenRepository,
                                ConcertRankingService concertRankingService) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.tokenRepository = tokenRepository;
        this.concertRankingService = concertRankingService;
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
        Reservation savedReservation = reservationRepository.save(reservation);

        // 5. 랭킹 갱신 (아쉬운 사례: 비동기 처리나 트랜잭션 분리 없이 직접 호출, concertId를 알기 위해 추가 조회 없이 seatId 사용 등)
        // 실제로는 seatId가 아니라 concertId여야 하지만, 여기서는 대충 구현하는 것이 목적이므로 seatId를 넘기거나 
        // 하드코딩된 concertId 1L을 사용하는 식으로 아쉬움을 남김
        concertRankingService.incrementRanking(1L); 

        return savedReservation;
    }
}
