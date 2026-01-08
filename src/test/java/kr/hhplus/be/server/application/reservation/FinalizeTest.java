package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import kr.hhplus.be.server.infrastructure.concert.SeatJpaRepository;
import kr.hhplus.be.server.infrastructure.concert.SeatEntity;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FinalizeTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatInteractor reserveSeatInteractor;

    @Autowired
    private SeatJpaRepository seatJpaRepository;

    @Autowired
    private ReservationTokenRepositoryPort tokenRepository;

    @Test
    @DisplayName("좌석 예약 기능 정상 흐름 테스트 - 예외 케이스 부족")
    void reserveSeatSuccessTest() {
        // given
        // 1번 좌석이 AVAILABLE 상태인 엔티티가 이미 DB에 있다고 가정
        SeatEntity seat = new SeatEntity();
        seat.setConcertId(1L);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setPrice(10000L);
        seatJpaRepository.save(seat);
        Long seatId = seat.getId();

        String tokenValue = UUID.randomUUID().toString();
        ReservationToken token = ReservationToken.create(tokenValue, 1L);
        tokenRepository.save(token);

        // when
        Reservation result = reserveSeatInteractor.reserve(new ReserveSeatUseCase.Command(1L, seatId, tokenValue));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSeatId()).isEqualTo(seatId);
        
        SeatEntity updatedSeat = seatJpaRepository.findById(seatId).get();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }
}
