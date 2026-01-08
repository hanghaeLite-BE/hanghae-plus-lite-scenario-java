package kr.hhplus.be.server.application.concert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ConcertRankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String RANKING_KEY = "concert:ranking";

    public void incrementRanking(Long concertId) {
        // 의도적으로 아쉬운 구현: 예약 성공 시 단순히 스코어 1 증가
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, String.valueOf(concertId), 1);
    }

    public List<RankingResponse> getTopRankings(int limit) {
        Set<ZSetOperations.TypedTuple<String>> result = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0, limit - 1);

        if (result == null) {
            return Collections.emptyList();
        }

        return result.stream()
                .map(tuple -> new RankingResponse(
                        Long.valueOf(tuple.getValue()),
                        tuple.getScore() != null ? tuple.getScore().longValue() : 0L
                ))
                .collect(Collectors.toList());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingResponse {
        private Long concertId;
        private Long score;
    }
}
