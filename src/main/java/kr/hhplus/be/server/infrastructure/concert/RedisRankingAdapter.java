package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.application.concert.ConcertRankingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisRankingAdapter implements ConcertRankingPort {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RANKING_KEY = "ranking:soldout_speed";
    private static final String SALES_KEY_PREFIX = "concert:%d:sales";
    private static final String START_AT_KEY = "concert:%d:sales:salesStartAt";
    private static final String SOLD_OUT_AT_KEY = "concert:%d:sales:soldOutAt";

    @Override
    public void updateSalesInfo(Long concertId, Long confirmedCount, Long totalSeats) {
        String salesKey = String.format(SALES_KEY_PREFIX, concertId);
        String startAtKey = String.format(START_AT_KEY, concertId);
        String soldOutAtKey = String.format(SOLD_OUT_AT_KEY, concertId);

        long currentTime = System.currentTimeMillis();

        // 1. 최초 결제 시점을 판매 시작 시점으로 기록 (setIfAbsent)
        redisTemplate.opsForValue().setIfAbsent(startAtKey, String.valueOf(currentTime), Duration.ofDays(7));

        // 2. 현재 누적 판매량 기록 (SET - DB 정합성 유지)
        redisTemplate.opsForHash().put(salesKey, "confirmedCount", String.valueOf(confirmedCount));
        redisTemplate.opsForHash().put(salesKey, "totalSeats", String.valueOf(totalSeats));
        redisTemplate.expire(salesKey, Duration.ofDays(7));

        // 3. 매진 여부 확인 및 매진 시점 기록
        if (confirmedCount >= totalSeats) {
            // setIfAbsent 성공 시에만 랭킹에 반영하여 1회성 보장
            Boolean success = redisTemplate.opsForValue().setIfAbsent(soldOutAtKey, String.valueOf(currentTime), Duration.ofDays(7));
            if (Boolean.TRUE.equals(success)) {
                String startAtStr = redisTemplate.opsForValue().get(startAtKey);
                if (startAtStr != null) {
                    long startAt = Long.parseLong(startAtStr);
                    long duration = currentTime - startAt;
                    // 랭킹 등록
                    redisTemplate.opsForZSet().add(RANKING_KEY, String.valueOf(concertId), duration);
                    // 랭킹 키에도 TTL 설정 (이미 설정되어 있을 수 있음)
                    redisTemplate.expire(RANKING_KEY, Duration.ofDays(30));
                }
            }
        }
    }

    @Override
    public List<RankingResponse> getTopRankings(int limit) {
        Set<ZSetOperations.TypedTuple<String>> result = redisTemplate.opsForZSet()
                .rangeWithScores(RANKING_KEY, 0, limit - 1); // 소요시간 짧은 순 (오름차순)

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
}
