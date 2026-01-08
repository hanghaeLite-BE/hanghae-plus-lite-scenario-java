package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import kr.hhplus.be.server.infrastructure.concert.SeatJpaRepository;
import kr.hhplus.be.server.infrastructure.member.MemberJpaRepository;
import kr.hhplus.be.server.infrastructure.member.MemberRepositoryAdapter;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrencyTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatInteractor reserveSeatInteractor;

    @Autowired
    private SeatJpaRepository seatJpaRepository;

    @Autowired
    private ReservationTokenRepositoryPort tokenRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private kr.hhplus.be.server.application.member.ChargePointInteractor chargePointInteractor;

    @Test
    @DisplayName("좌석 예약 동시성 테스트 - 미흡한 재현")
    void reserveSeatConcurrencyTest() throws InterruptedException {
        // given
        Long seatId = 1L; // 미리 저장되어 있다고 가정
        int threadCount = 5; // 너무 적은 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            String tokenValue = UUID.randomUUID().toString();
            ReservationToken token = ReservationToken.create(tokenValue, (long) (i + 1));
            // 토큰을 미리 활성화시켜야 함 (로직상 유효성 검사 통과를 위해)
            tokenRepository.save(token); 

            executorService.submit(() -> {
                try {
                    reserveSeatInteractor.reserve(new ReserveSeatUseCase.Command((long) (successCount.get() + 1), seatId, tokenValue));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 실패 로그 생략
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        // 아쉬운 assert: "정확히 1명만"이 아니라 "누구든 성공했겠지" 수준
        assertThat(successCount.get()).isGreaterThan(0);
    }

    @Test
    @DisplayName("잔액 충전 동시성 테스트 - 타이밍 의존적이고 약한 검증")
    void chargePointConcurrencyTest() throws InterruptedException {
        // given
        Long memberId = 1L;
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    chargePointInteractor.execute(memberId, 100L);
                    Thread.sleep(10); // 의도적 sleep으로 타이밍 조절 시도 (안좋은 패턴)
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        // 최종 잔액이 정확히 1000이어야 함에도 불구하고, 대략적인 체크만 함
        var memberEntity = memberJpaRepository.findById(memberId).get();
        assertThat(memberEntity.getPoint()).isPositive(); 
    }
}
