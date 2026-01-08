package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import kr.hhplus.be.server.infrastructure.RedisLockManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReserveSeatInteractor implements ReserveSeatUseCase {

    private final SeatRepositoryPort seatRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final ReservationTokenRepositoryPort tokenRepository;
    private final RedisLockManager redisLockManager;

    public ReserveSeatInteractor(SeatRepositoryPort seatRepository,
                                ReservationRepositoryPort reservationRepository,
                                ReservationTokenRepositoryPort tokenRepository,
                                RedisLockManager redisLockManager) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.tokenRepository = tokenRepository;
        this.redisLockManager = redisLockManager;
    }

    /**
     * Facade 메서드에서 분산락을 획득하고 내부 트랜잭션 로직을 호출 (아쉬운 사례 재현)
     */
    @Override
    public Reservation reserve(Command command) {
        // (1) 분산락 키/범위 선정이 어설픔: 요청에 포함된 좌석들 중 첫 번째 좌석에 대해서만 락
        // 락 키는 단순히 "seat:{seatId}" 수준, TTL은 짧게(3초) 고정
        String lockKey = "seat:" + command.seatId();
        
        if (!redisLockManager.tryLock(lockKey, 3)) {
            // 락 획득 실패 시 런타임 예외 발생
            throw new IllegalStateException("잠시 후 다시 시도해주세요. (Lock Acquisition Failed)");
        }

        try {
            // (3) 트랜잭션과 락의 조합이 형식적으로만 들어감 (Facade에서 락을 잡고 내부 @Transactional 호출)
            return reserveWithTransaction(command);
        } finally {
            // try/finally로 unlock은 수행
            redisLockManager.unlock(lockKey);
        }
    }

    @Transactional
    public Reservation reserveWithTransaction(Command command) {
        // 1. 토큰 검증
        ReservationToken token = tokenRepository.findByToken(command.token())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
        
        if (!token.isValid()) {
            throw new IllegalStateException("만료되었거나 이미 사용된 토큰입니다.");
        }

        // 2. 좌석 선점 (DB 원자적 업데이트 활용 - 락과 별개로 안전장치로 유지)
        boolean success = seatRepository.reserveAtomically(
                command.seatId(), 
                LocalDateTime.now().plusMinutes(5)
        );

        if (!success) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }

        // 3. 토큰 사용 처리
        token.use();
        tokenRepository.save(token);

        // 4. 예약 레코드 생성
        Reservation reservation = Reservation.create(command.userId(), command.seatId());
        return reservationRepository.save(reservation);
    }
}
