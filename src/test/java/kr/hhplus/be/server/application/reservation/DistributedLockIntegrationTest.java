package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.application.concert.ConcertRepositoryPort;
import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class DistributedLockIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatDistributedLockFacade reserveSeatDistributedLockFacade;

    @Autowired
    private ConfirmReservationDistributedLockFacade confirmReservationDistributedLockFacade;

    @Autowired
    private SeatRepositoryPort seatRepositoryPort;

    @Autowired
    private MemberRepositoryPort memberRepositoryPort;

    @Autowired
    private ReservationRepositoryPort reservationRepositoryPort;

    @Test
    @DisplayName("TC-LOCK-001: 동일 좌석에 대해 50개 동시 예약 시 1명만 성공해야 한다")
    void shouldSuccessOnlyOneWhenConcurrentReserve() throws Exception {
        // given
        Long concertId = 1L;
        Long seatId = 1L;
        // 테스트용 좌석 상태 초기화 (사용 가능 상태로)
        Seat seat = seatRepositoryPort.findById(seatId).orElseThrow();
        seatRepositoryPort.save(new Seat(seat.getId(), seat.getConcertId(), seat.getSeatNumber(), seat.getPrice(), SeatStatus.AVAILABLE, null));

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1; // 가상의 유저 ID
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    reserveSeatDistributedLockFacade.reserveSeat(concertId, seatId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        
        Seat finalSeat = seatRepositoryPort.findById(seatId).orElseThrow();
        assertThat(finalSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }
}
