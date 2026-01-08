package kr.hhplus.be.server.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class RedisLockManager {

    private final StringRedisTemplate redisTemplate;

    public RedisLockManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @param lockKey Lock key
     * @param ttl     Time to live (Duration)
     * @return Holder ID if lock is acquired, null otherwise
     */
    public String acquireLock(String lockKey, Duration ttl) {
        String lockValue = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, ttl);
        return Boolean.TRUE.equals(success) ? lockValue : null;
    }

    /**
     * Lua 스크립트를 사용하지 않는 버전의 락 해제
     * - Get 후 ID가 일치하면 Delete 수행
     * - 주의: Get과 Delete 사이에 원자성이 보장되지 않으므로, 극히 드문 확률로 다른 클라이언트의 락을 해제할 위험이 있음.
     * - 튜터 의견: 그러나 협업하는 인원이 모두 Lua 스크립트 사용에 익숙하지 않을 수 있으므로, 가능하면 Lua 스크립트를 사용하지 않는 방향을 선호함.
     * 
     * @param lockKey  Lock key
     * @param holderId The ID returned from acquireLock
     */
    public void releaseLock(String lockKey, String holderId) {
        if (holderId == null) return;

        String currentValue = redisTemplate.opsForValue().get(lockKey);
        if (holderId.equals(currentValue)) {
            redisTemplate.delete(lockKey);
        }
    }
}
