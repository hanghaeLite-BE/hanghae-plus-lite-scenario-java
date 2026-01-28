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

        // [STEP9 변환] 4. 예약 확정 이벤트를 Kafka로 발행
        // STEP8: Application Event + @TransactionalEventListener(AFTER_COMMIT)
        // └─ 프로세스 내부 이벤트 (트랜잭션과 외부 호출 분리)
        //
        // STEP9: Kafka 메시지 발행 (일반 사례)
        // └─ 프로세스 외부 메시지 브로커 (분산 시스템에서 신뢰할 수 있는 전송)
        //
        // 장점:
        // 1. 여러 서비스가 같은 이벤트 구독 가능 (확장성)
        // 2. 메시지 히스토리 보존 (감사 추적)
        // 3. 비동기 처리로 응답 시간 단축
        // 
        // 기초 수준 구현 (개선 사례에서 고도화):
        // - Fire-and-forget (재시도 로직 없음)
        // - 단순 JSON 직렬화
        // - Consumer 실패 시 로그만 기록
        
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
        
        reservationEventProducer.publishReservationCompleted(kafkaMessage);
        
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
