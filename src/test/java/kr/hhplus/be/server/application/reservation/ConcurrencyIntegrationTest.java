package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import kr.hhplus.be.server.domain.reservation.TokenStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrencyIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    private ConfirmReservationUseCase confirmReservationUseCase;

    @Autowired
    private SeatRepositoryPort seatRepository;

    @Autowired
    private MemberRepositoryPort memberRepository;

    @Autowired
    private ReservationRepositoryPort reservationRepository;

    @Autowired
    private ReservationTokenRepositoryPort tokenRepository;

    @Test
    @DisplayName("동일 좌석에 대해 동시에 10명이 예약을 시도하면 1명만 성공해야 한다")
    void reserveSeatConcurrencyTest() throws InterruptedException {
        // given
        Long seatId = 1L; // 초기화 스크립트나 데이터가 있다고 가정하거나 여기서 생성
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // 토큰들을 미리 준비
        String[] tokens = new String[threadCount];
        for (int i = 0; i < threadCount; i++) {
            String tokenValue = UUID.randomUUID().toString();
            ReservationToken token = ReservationToken.builder()
                    .token(tokenValue)
                    .userId((long) (i + 1))
                    .status(TokenStatus.ACTIVE)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            tokenRepository.save(token);
            tokens[i] = tokenValue;
        }

        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executorService.submit(() -> {
                try {
                    reserveSeatUseCase.reserve(new ReserveSeatUseCase.Command(
                            (long) (idx + 1), seatId, tokens[idx]
                    ));
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
    }

    @Test
    @DisplayName("한 사용자가 동시에 여러 건의 결제를 시도해도 잔액은 정확히 차감되어야 한다 (음수 잔액 방지)")
    void confirmReservationConcurrencyTest() throws InterruptedException {
        // given
        Long userId = 100L;
        Long initialBalance = 10000L;
        Long seatPrice = 6000L;

        Member member = Member.builder()
                .id(userId)
                .points(initialBalance)
                .build();
        memberRepository.save(member);

        // 예약 2건 생성
        Seat seat1 = Seat.builder().id(101L).price(seatPrice).status(SeatStatus.RESERVED).build();
        Seat seat2 = Seat.builder().id(102L).price(seatPrice).status(SeatStatus.RESERVED).build();
        seatRepository.save(seat1);
        seatRepository.save(seat2);

        Reservation res1 = reservationRepository.save(Reservation.create(userId, 101L));
        Reservation res2 = reservationRepository.save(Reservation.create(userId, 102L));

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        executorService.submit(() -> {
            try {
                confirmReservationUseCase.confirm(new ConfirmReservationUseCase.Command(res1.getId(), userId));
                successCount.getAndIncrement();
            } catch (Exception e) {
                failCount.getAndIncrement();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                confirmReservationUseCase.confirm(new ConfirmReservationUseCase.Command(res2.getId(), userId));
                successCount.getAndIncrement();
            } catch (Exception e) {
                failCount.getAndIncrement();
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        // then
        // 잔액이 10000인데 6000원짜리 2개를 동시에 결제하면 1개만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        Member finalMember = memberRepository.findById(userId).get();
        assertThat(finalMember.getPoints()).isEqualTo(initialBalance - seatPrice);
        assertThat(finalMember.getPoints()).isGreaterThanOrEqualTo(0L);
    }
}
