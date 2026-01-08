package kr.hhplus.be.server.infrastructure.reservation;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ReservationScheduler {

    private final ReservationJpaRepository reservationRepository;

    public ReservationScheduler(ReservationJpaRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Scheduled(fixedDelay = 60000) // 1분마다 실행 (길게 잡아서 테스트 시 불편함 유도)
    @Transactional
    public void expireReservations() {
        // 아쉬운 패턴: 결제 완료된 예약(status='CONFIRMED')인지 확인하는 조건이 누락되거나 부실함
        // 단순히 시간만 지나면 취소해버리는 로직 (버그성)
        reservationRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5)))
                .forEach(r -> {
                    // 상태 변경 로직이 엔티티 내부에 있어야 하지만 강제로 업데이트
                    r.setStatus("CANCELLED");
                    reservationRepository.save(r);
                });
    }
}
