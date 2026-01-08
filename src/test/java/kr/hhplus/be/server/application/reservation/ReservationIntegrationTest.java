package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import kr.hhplus.be.server.domain.reservation.TokenStatus;
import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    private ConfirmReservationUseCase confirmReservationUseCase;

    @Autowired
    private ReservationTokenRepositoryPort tokenRepository;

    @Autowired
    private SeatRepositoryPort seatRepository;

    @Autowired
    private MemberRepositoryPort memberRepository;

    @Autowired
    private ReservationRepositoryPort reservationRepository;

    @Autowired
    private PaymentRepositoryPort paymentRepository;

    @Test
    @DisplayName("만료 후 좌석 재예약 가능 테스트")
    void seat_re_reservation_after_expiration_test() {
        // given
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long concertId = 1L;
        Long seatId = 2L;

        // 1. 유효한 토큰들 발급
        ReservationToken token1 = ReservationToken.builder().token("token-1").userId(userId1).concertId(concertId).status(TokenStatus.ISSUED).expiresAt(LocalDateTime.now().plusHours(1)).createdAt(LocalDateTime.now()).build();
        ReservationToken token2 = ReservationToken.builder().token("token-2").userId(userId2).concertId(concertId).status(TokenStatus.ISSUED).expiresAt(LocalDateTime.now().plusHours(1)).createdAt(LocalDateTime.now()).build();
        tokenRepository.save(token1);
        tokenRepository.save(token2);

        // 2. 이미 예약되었으나 곧 만료될 좌석 생성 (과거 시간으로 reservedUntil 설정)
        Seat seat = new Seat(seatId, concertId, 2, SeatStatus.RESERVED, 5000L, LocalDateTime.now().minusMinutes(1));
        seatRepository.save(seat);

        // when
        // 만료된 좌석에 대해 다른 유저가 예약 요청
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId2, concertId, seatId, "token-2");
        Reservation reservation = reserveSeatUseCase.reserve(reserveCommand);

        // then
        assertThat(reservation).isNotNull();
        Seat updatedSeat = seatRepository.findById(seatId).get();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(updatedSeat.getReservedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("동시 좌석 예약 요청 시 한 명만 성공 테스트")
    void concurrent_seat_reservation_test() throws InterruptedException {
        // given
        int threadCount = 10;
        Long concertId = 1L;
        Long seatId = 3L;
        
        // 좌석 생성
        Seat seat = new Seat(seatId, concertId, 3, SeatStatus.AVAILABLE, 5000L, null);
        seatRepository.save(seat);

        // 여러 명의 유저와 토큰 생성
        for (long i = 1; i <= threadCount; i++) {
            ReservationToken token = ReservationToken.builder()
                    .token("concurrent-token-" + i)
                    .userId(i)
                    .concertId(concertId)
                    .status(TokenStatus.ISSUED)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .createdAt(LocalDateTime.now())
                    .build();
            tokenRepository.save(token);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (long i = 1; i <= threadCount; i++) {
            final long userId = i;
            final String token = "concurrent-token-" + i;
            executorService.execute(() -> {
                try {
                    reserveSeatUseCase.reserve(new ReserveSeatUseCase.Command(userId, concertId, seatId, token));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        Seat finalSeat = seatRepository.findById(seatId).get();
        assertThat(finalSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    @DisplayName("토큰 발급 -> 좌석 예약 -> 결제 완료 E2E 흐름 테스트")
    void reservation_e2e_test() {
>>>>+++ REPLACE

        // given
        Long userId = 1L;
        Long concertId = 1L;
        Long seatId = 1L;
        
        // 1. 유저 생성 및 포인트 충전
        Member member = new Member(userId, "User1", 10000L);
        memberRepository.save(member);

        // 2. 토큰 발급
        ReservationToken token = ReservationToken.builder()
                .token("test-token-123")
                .userId(userId)
                .concertId(concertId)
                .status(TokenStatus.ISSUED)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .build();
        tokenRepository.save(token);

        // 3. 좌석 생성
        Seat seat = new Seat(seatId, concertId, 1, SeatStatus.AVAILABLE, 5000L, null);
        seatRepository.save(seat);

        // when
        // Step 1: 좌석 예약
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId, concertId, seatId, "test-token-123");
        Reservation reservation = reserveSeatUseCase.reserve(reserveCommand);

        // Step 2: 결제 및 예약 확정
        ConfirmReservationUseCase.Command confirmCommand = new ConfirmReservationUseCase.Command(reservation.getId(), userId);
        confirmReservationUseCase.confirm(confirmCommand);

        // then
        // 1. 좌석 상태 확인
        Seat updatedSeat = seatRepository.findById(seatId).get();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.SOLD);

        // 2. 예약 상태 확인
        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).get();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        // 3. 유저 잔액 확인
        Member updatedMember = memberRepository.findById(userId).get();
        assertThat(updatedMember.getPoint()).isEqualTo(5000L);

        // 4. 토큰 상태 확인 (사용 완료 정책에 따라 다를 수 있으나 요구사항에 따라 USED 처리 확인)
        ReservationToken updatedToken = tokenRepository.findByToken("test-token-123").get();
        assertThat(updatedToken.getStatus()).isEqualTo(TokenStatus.USED);
    }
}
