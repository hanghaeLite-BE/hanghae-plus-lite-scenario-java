package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatSchedulerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private SeatExpirationScheduler seatExpirationScheduler;

    @Autowired
    private SeatRepositoryPort seatRepository;

    @Test
    @DisplayName("만료된 점유 좌석이 스케줄러에 의해 AVAILABLE 상태로 복구되어야 한다")
    void releaseExpiredSeatsTest() {
        // given
        Long seatId = 200L;
        Seat expiredSeat = Seat.builder()
                .id(seatId)
                .status(SeatStatus.RESERVED)
                .reservedUntil(LocalDateTime.now().minusMinutes(1)) // 이미 만료됨
                .price(5000L)
                .build();
        seatRepository.save(expiredSeat);

        // when
        seatExpirationScheduler.releaseExpiredSeats();

        // then
        Seat releasedSeat = seatRepository.findById(seatId).get();
        assertThat(releasedSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(releasedSeat.getReservedUntil()).isNull();
    }

    @Test
    @DisplayName("만료되지 않은 점유 좌석은 스케줄러에 의해 해제되지 않아야 한다")
    void shouldNotReleaseValidSeatsTest() {
        // given
        Long seatId = 201L;
        Seat validSeat = Seat.builder()
                .id(seatId)
                .status(SeatStatus.RESERVED)
                .reservedUntil(LocalDateTime.now().plusMinutes(5)) // 아직 유효함
                .price(5000L)
                .build();
        seatRepository.save(validSeat);

        // when
        seatExpirationScheduler.releaseExpiredSeats();

        // then
        Seat seatAfter = seatRepository.findById(seatId).get();
        assertThat(seatAfter.getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(seatAfter.getReservedUntil()).isNotNull();
    }
}
