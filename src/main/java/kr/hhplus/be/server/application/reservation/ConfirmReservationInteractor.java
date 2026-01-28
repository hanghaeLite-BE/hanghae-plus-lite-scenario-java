package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.concert.ConcertRankingService;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationCompletedEvent;
import kr.hhplus.be.server.infrastructure.kafka.ReservationEventMessage;
import kr.hhplus.be.server.infrastructure.kafka.ReservationEventProducer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConfirmReservationInteractor implements ConfirmReservationUseCase {

    private final ReservationRepositoryPort reservationRepository;
    private final MemberRepositoryPort memberRepository;
    private final SeatRepositoryPort seatRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final ConcertRankingService concertRankingService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ReservationEventProducer reservationEventProducer;

    public ConfirmReservationInteractor(ReservationRepositoryPort reservationRepository,
                                        MemberRepositoryPort memberRepository,
                                        SeatRepositoryPort seatRepository,
                                        PaymentRepositoryPort paymentRepository,
                                        ConcertRankingService concertRankingService,
                                        ApplicationEventPublisher applicationEventPublisher,
                                        ReservationEventProducer reservationEventProducer) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.seatRepository = seatRepository;
        this.paymentRepository = paymentRepository;
        this.concertRankingService = concertRankingService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.reservationEventProducer = reservationEventProducer;
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

        // [STEP9 개선 사례] 4. 예약 확정 이벤트를 Kafka로 발행 (커밋 이후 보장)
        // 
        // TransactionSynchronizationManager를 사용하여 다음을 보장한다:
        // 1. 트랜잭션 커밋 이후에만 Kafka 메시지 발행
        // 2. 롤백 시 메시지 발행 취소 (정합성)
        // 3. 메시지 발행 실패가 도메인 트랜잭션에 영향 없음
        //
        // Topic: concert.reservation.completed.v1 (버전 포함)
        // Key: reservationId (같은 예약은 같은 partition)
        // Payload: eventId, reservationId, userId, concertId, seatId, paidAmount, occurredAt
        
        String eventId = UUID.randomUUID().toString();
        ReservationEventMessage kafkaMessage = ReservationEventMessage.builder()
                .eventId(eventId)
                .reservationId(reservation.getId())
                .userId(member.getId())
                .concertId(seat.getConcertId())
                .seatId(seat.getId())
                .paidAmount(seat.getPrice())
                .occurredAt(LocalDateTime.now().toString())
                .build();
        
        // [개선] 새로운 메서드: 커밋 이후 발행 보장
        reservationEventProducer.publishReservationCompletedAfterCommit(kafkaMessage);
        
        // [보존] Application Event는 아직 유지 (기존 STEP8 리스너가 있을 경우 호환)
        // 개선 사례에서는 완전히 제거할 수 있음
        ReservationCompletedEvent event = new ReservationCompletedEvent(
                this,
                reservation.getId(),
                member.getId(),
                seat.getConcertId(),
                seat.getId(),
                seat.getPrice(),
                LocalDateTime.now()
        );
        applicationEventPublisher.publishEvent(event);

        // 5. 랭킹 정보 업데이트 트리거
        // DB 정합성 기준 (Source of Truth): 현재까지 확정 판매된 좌석 수를 조회하여 Redis 갱신
        long confirmedCount = seatRepository.countConfirmedSeatsByConcertId(seat.getConcertId());
        long totalSeats = seatRepository.countTotalSeatsByConcertId(seat.getConcertId());
        concertRankingService.updateSalesInfo(seat.getConcertId(), confirmedCount, totalSeats);
    }
}
