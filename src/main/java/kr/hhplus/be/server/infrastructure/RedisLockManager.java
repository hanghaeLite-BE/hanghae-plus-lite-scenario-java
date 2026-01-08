package kr.hhplus.be.server.infrastructure;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockManager {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisLockManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 단순 SETNX + TTL 기반 락 획득
     * 소유자 정보(Token)를 검증하지 않는 단순한 구현 (아쉬운 사례 재현)
     */
    public boolean tryLock(String key, long timeoutSeconds) {
        // value는 단순히 "lock" 상수로 고정
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(key, "lock", Duration.ofSeconds(timeoutSeconds)));
    }

    /**
     * 단순 DEL 기반 락 해제
     * 소유자 확인 없이 무조건 삭제 (아쉬운 사례 재현)
     */
    public void unlock(String key) {
        // TTL 만료 후 다른 요청이 락을 획득했을 때, 이전 요청이 unlock을 호출하면 남의 락을 해제할 위험이 있음
        redisTemplate.delete(key);
    }
}
