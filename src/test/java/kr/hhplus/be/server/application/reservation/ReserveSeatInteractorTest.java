package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReserveSeatInteractorTest {

    @Mock
    private SeatRepositoryPort seatRepository;

    @Mock
    private ReservationRepositoryPort reservationRepository;

    @InjectMocks
    private ReserveSeatInteractor reserveSeatInteractor;

    @Test
    @DisplayName("[성공] AVAILABLE 좌석 예약 시 Seat status가 RESERVED로 변경되고 Reservation이 생성된다")
    void reserve_success() {
        // given
        Long memberId = 1L;
        Long seatId = 100L;
        Seat seat = new Seat(seatId, 1L, 10, SeatStatus.AVAILABLE, 50000L);

        given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));
        given(reservationRepository.save(any(Reservation.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Reservation result = reserveSeatInteractor.execute(memberId, seatId);

        // then
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(result.getMemberId()).isEqualTo(memberId);
        assertThat(result.getSeatId()).isEqualTo(seatId);
        verify(seatRepository).save(seat);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("[실패] 이미 RESERVED된 좌석 예약 시도시 실패한다")
    void reserve_fail_already_reserved() {
        // given
        Long memberId = 1L;
        Long seatId = 100L;
        Seat seat = new Seat(seatId, 1L, 10, SeatStatus.RESERVED, 50000L);

        given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));

        // when & then
        assertThatThrownBy(() -> reserveSeatInteractor.execute(memberId, seatId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("좌석이 예약 가능한 상태가 아닙니다.");
    }
}
