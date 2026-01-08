package kr.hhplus.be.server.application.concert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SeatExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeatExpirationScheduler.class);

    private final SeatRepositoryPort seatRepository;

    public SeatExpirationScheduler(SeatRepositoryPort seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void releaseExpiredSeats() {
        log.info("만료된 좌석 해제 스케줄러 실행");
        int releasedCount = seatRepository.releaseExpiredSeats(LocalDateTime.now());
        if (releasedCount > 0) {
            log.info("만료된 좌석 {}개 해제 완료", releasedCount);
        }
    }
}
