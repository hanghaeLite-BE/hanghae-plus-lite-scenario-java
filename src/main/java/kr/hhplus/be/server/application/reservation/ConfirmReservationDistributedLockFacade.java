package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.infrastructure.redis.RedisLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmReservationDistributedLockFacade {

    private final RedisLockManager redisLockManager;
    private final ConfirmReservationUseCase confirmReservationUseCase;

    public void confirmReservation(Long reservationId, Long userId) {
        String lockKey = "lock:reservation:" + reservationId;
        // TTL 10초: 결제 처리가 외부 API를 포함할 수도 있음을 고려 (기획 협의 시나리오)
        String holderId = redisLockManager.acquireLock(lockKey, Duration.ofSeconds(10));
        
        if (holderId == null) {
            throw new RuntimeException("현재 결제 처리가 진행 중입니다.");
        }

        try {
            confirmReservationUseCase.confirm(new ConfirmReservationUseCase.Command(reservationId, userId));
        } finally {
            redisLockManager.releaseLock(lockKey, holderId);
        }
    }
}
