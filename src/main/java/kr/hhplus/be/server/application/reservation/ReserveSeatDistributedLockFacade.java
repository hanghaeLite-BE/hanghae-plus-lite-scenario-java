package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.infrastructure.redis.RedisLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveSeatDistributedLockFacade {

    private final RedisLockManager redisLockManager;
    private final ReserveSeatUseCase reserveSeatUseCase;

    public kr.hhplus.be.server.domain.reservation.Reservation reserveSeat(Long concertId, Long seatId, Long userId, String token) {
        String lockKey = "lock:seat:" + concertId + ":" + seatId;
        // TTL 5초: 트랜잭션 및 네트워크 대기 시간을 고려하여 설정 (기획/기술 협의 가정)
        String holderId = redisLockManager.acquireLock(lockKey, Duration.ofSeconds(5));
        
        if (holderId == null) {
            throw new RuntimeException("이미 처리 중인 좌석입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            return reserveSeatUseCase.reserve(new ReserveSeatUseCase.Command(userId, seatId, token));
        } finally {
            redisLockManager.releaseLock(lockKey, holderId);
        }
    }
}
