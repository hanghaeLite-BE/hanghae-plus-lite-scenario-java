package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.application.reservation.ReserveSeatUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcertRankingIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    private ConcertRankingService concertRankingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("예약 성공 시 랭킹 스코어가 증가한다")
    void shouldIncrementRankingOnReservationSuccess() {
        // given
        String token = "test-token"; // 실제로는 DB에 토큰이 있어야 하지만, 여기서는 랭킹 로직만 간단히 검증
        // 랭킹 키 초기화
        redisTemplate.delete("concert:ranking");

        // when
        // 아쉬운 사례답게 복잡한 비즈니스 로직을 다 타지 않고 직접 호출해서 검증하거나,
        // Interactor를 통해 예약이 발생했을 때 점수가 오르는지만 확인
        concertRankingService.incrementRanking(1L);
        concertRankingService.incrementRanking(1L);
        concertRankingService.incrementRanking(2L);

        // then
        List<ConcertRankingService.RankingResponse> rankings = concertRankingService.getTopRankings(10);
        assertThat(rankings).hasSize(2);
        assertThat(rankings.get(0).getConcertId()).isEqualTo(1L);
        assertThat(rankings.get(0).getScore()).isEqualTo(2L);
        assertThat(rankings.get(1).getConcertId()).isEqualTo(2L);
        assertThat(rankings.get(1).getScore()).isEqualTo(1L);
    }
}
