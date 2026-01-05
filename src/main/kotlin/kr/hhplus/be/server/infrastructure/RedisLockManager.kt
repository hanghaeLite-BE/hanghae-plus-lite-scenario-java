package kr.hhplus.be.server.infrastructure

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Redis 기반 분산락 매니저
 *
 * 단순한 SET NX + TTL 방식으로 구현했습니다.
 * - 락 소유자 식별 없음
 * - TTL 만료 시나리오에 대한 방어 없음
 *
 * 이는 교육용 PR의 일부로, 실제 운영 환경에서는 추가적인 안전장치가 필요합니다.
 */
@Component
class RedisLockManager(
    private val redisTemplate: StringRedisTemplate,
) {
    /**
     * 분산락 획득 시도
     *
     * @param key 락 키
     * @param timeoutMs 락 유지 시간 (밀리초)
     * @return 락 획득 성공 여부
     */
    fun tryLock(key: String, timeoutMs: Long): Boolean {
        val result =
            redisTemplate.opsForValue()
                .setIfAbsent(key, "lock", timeoutMs, TimeUnit.MILLISECONDS)
        return java.lang.Boolean.TRUE == result
    }

    /**
     * 분산락 해제
     *
     * 주의: 현재 구현은 락 소유자 검증 없이 삭제합니다.
     * TTL 만료 후 다른 요청이 락을 획득한 경우에도 기존 요청이 unlock 할 수 있습니다.
     */
    fun unlock(key: String) {
        redisTemplate.delete(key)
    }
}
