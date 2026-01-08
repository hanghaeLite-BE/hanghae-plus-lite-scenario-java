package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConfirmReservationInteractorTest {

    @Mock
    private ReservationRepositoryPort reservationRepository;

    @Mock
    private MemberRepositoryPort memberRepository;

    @Mock
    private SeatRepositoryPort seatRepository;

    @InjectMocks
    private ConfirmReservationInteractor confirmReservationInteractor;

    @Test
    @DisplayName("[성공] 포인트가 충분하면 결제가 완료되고 예약/좌석 상태가 확정/매진으로 변경된다")
    void confirm_success() {
        // given
        Long memberId = 1L;
        Long seatId = 100L;
        Long reservationId = 50L;
        Long price = 50000L;

        Reservation reservation = new Reservation(reservationId, memberId, seatId, ReservationStatus.RESERVED, LocalDateTime.now());
        Member member = new Member(memberId, 100000L); // 10만 포인트
        Seat seat = new Seat(seatId, 1L, 10, SeatStatus.RESERVED, price);

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));

        // when
        confirmReservationInteractor.execute(reservationId, memberId);

        // then
        assertThat(member.getPointBalance()).isEqualTo(50000L); // 10만 - 5만
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD);

        verify(memberRepository).save(member);
        verify(reservationRepository).save(reservation);
        verify(seatRepository).save(seat);
    }

    @Test
    @DisplayName("[실패] 포인트가 부족하면 결제에 실패한다")
    void confirm_fail_insufficient_points() {
        // given
        Long memberId = 1L;
        Long seatId = 100L;
        Long reservationId = 50L;
        Long price = 50000L;

        Reservation reservation = new Reservation(reservationId, memberId, seatId, ReservationStatus.RESERVED, LocalDateTime.now());
        Member member = new Member(memberId, 30000L); // 3만 포인트 (부족)
        Seat seat = new Seat(seatId, 1L, 10, SeatStatus.RESERVED, price);

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));

        // when & then
        assertThatThrownBy(() -> confirmReservationInteractor.execute(reservationId, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("포인트 잔액이 부족합니다.");

        // 상태 불변 검증
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    @DisplayName("[실패] 이미 CONFIRMED된 중복 결제 시도시 실패한다")
    void confirm_fail_already_confirmed() {
        // given
        Long memberId = 1L;
        Long reservationId = 50L;

        Reservation reservation = new Reservation(reservationId, memberId, 100L, ReservationStatus.CONFIRMED, LocalDateTime.now());

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> confirmReservationInteractor.execute(reservationId, memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 확정되었거나 확정할 수 없는 예약입니다.");
    }
}
