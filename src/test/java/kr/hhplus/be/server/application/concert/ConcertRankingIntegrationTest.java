package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.application.reservation.ConfirmReservationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcertRankingIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ConcertRankingService concertRankingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("동시에 여러 건의 결제 확정이 발생해도 매진 시점과 랭킹 등록은 1회만 발생해야 한다")
    void shouldRegisterRankingOnlyOnceOnConcurrentConfirmations() throws InterruptedException {
        // given
        Long concertId = 1L;
        Long totalSeats = 10L;
        Long confirmedCount = 10L; // 매진 상태라고 가정
        
        redisTemplate.delete("ranking:soldout_speed");
        redisTemplate.delete("concert:1:sales:salesStartAt");
        redisTemplate.delete("concert:1:sales:soldOutAt");
        redisTemplate.delete("concert:1:sales");

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 동일한 매진 시점 갱신 요청을 여러 스레드에서 동시에 수행
                    concertRankingService.updateSalesInfo(concertId, confirmedCount, totalSeats);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        // 1. 랭킹에 등록된 데이터는 1개여야 함
        List<ConcertRankingPort.RankingResponse> rankings = concertRankingService.getTopRankings(10);
        assertThat(rankings).hasSize(1);
        assertThat(rankings.get(0).getConcertId()).isEqualTo(concertId);

        // 2. soldOutAt 키는 1회만 설정되어 있어야 함 (setIfAbsent 특성)
        String soldOutAt = redisTemplate.opsForValue().get("concert:1:sales:soldOutAt");
        assertThat(soldOutAt).isNotNull();
    }
}
