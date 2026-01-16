package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.concert.ConcertRankingService;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.infrastructure.external.DataPlatformClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConfirmReservationInteractor implements ConfirmReservationUseCase {

    private final ReservationRepositoryPort reservationRepository;
    private final MemberRepositoryPort memberRepository;
    private final SeatRepositoryPort seatRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final ConcertRankingService concertRankingService;
    private final DataPlatformClient dataPlatformClient;

    public ConfirmReservationInteractor(ReservationRepositoryPort reservationRepository,
                                        MemberRepositoryPort memberRepository,
                                        SeatRepositoryPort seatRepository,
                                        PaymentRepositoryPort paymentRepository,
                                        ConcertRankingService concertRankingService,
                                        DataPlatformClient dataPlatformClient) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.seatRepository = seatRepository;
        this.paymentRepository = paymentRepository;
        this.concertRankingService = concertRankingService;
        this.dataPlatformClient = dataPlatformClient;
    }

    @Override
    @Transactional
    public void confirm(Command command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!reservation.getMemberId().equals(command.userId())) {
            throw new IllegalArgumentException("본인의 예약만 확정할 수 있습니다.");
        }

        // 비관적 락 사용
        Member member = memberRepository.findByIdWithLock(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 1. 포인트 차감
        member.usePoints(seat.getPrice());
        memberRepository.save(member);

        // 2. 예약 및 좌석 상태 변경
        reservation.confirm();
        seat.confirm();

        reservationRepository.save(reservation);
        seatRepository.save(seat);

        // 3. 결제 레코드 생성
        Payment payment = Payment.builder()
                .reservationId(reservation.getId())
                .userId(member.getId())
                .amount(seat.getPrice())
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // [문제점] 4. 데이터 플랫폼 전송 (트랜잭션 내부에서 동기 호출)
        // - 외부 API 호출이 트랜잭션 내부에 섞여 있음
        // - 네트워크 지연이 전체 응답시간에 영향
        // - 외부 호출 실패가 DB 트랜잭션 롤백을 유발할 수 있음
        try {
            DataPlatformClient.ReservationEventPayload payload = 
                    new DataPlatformClient.ReservationEventPayload(
                            reservation.getId(),
                            member.getId(),
                            seat.getConcertId(),
                            seat.getId(),
                            seat.getPrice(),
                            LocalDateTime.now()
                    );
            dataPlatformClient.postReservationEvent(payload);
        } catch (Exception e) {
            // 문제점: 단순히 예외를 다시 던지면 전체 트랜잭션이 롤백됨
            // 하지만 데이터 플랫폼 전송 실패가 예약 확정을 무효화할 정도의 심각한 문제인가?
            // 이 부분이 개선되어야 함 (이벤트 기반 비동기 처리로)
            throw e;
        }

        // 5. 랭킹 정보 업데이트 트리거
        // DB 정합성 기준 (Source of Truth): 현재까지 확정 판매된 좌석 수를 조회하여 Redis 갱신
        long confirmedCount = seatRepository.countConfirmedSeatsByConcertId(seat.getConcertId());
        long totalSeats = seatRepository.countTotalSeatsByConcertId(seat.getConcertId());
        concertRankingService.updateSalesInfo(seat.getConcertId(), confirmedCount, totalSeats);
    }
}
