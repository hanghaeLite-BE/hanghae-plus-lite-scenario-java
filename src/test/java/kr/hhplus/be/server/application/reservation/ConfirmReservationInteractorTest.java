package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfirmReservationInteractorTest {

    @Mock
    private ReservationRepositoryPort reservationRepository;
    @Mock
    private MemberRepositoryPort memberRepository;
    @Mock
    private SeatRepositoryPort seatRepository;
    @Mock
    private PaymentRepositoryPort paymentRepository;

    @InjectMocks
    private ConfirmReservationInteractor confirmReservationInteractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("예약 확정 성공 테스트")
    void confirm_success() {
        // given
        Long reservationId = 1L;
        Long memberId = 1L;
        Long seatId = 1L;
        Long price = 5000L;

        Reservation reservation = new Reservation(reservationId, memberId, seatId, ReservationStatus.PENDING, LocalDateTime.now());
        Member member = new Member(memberId, 10000L);
        Seat seat = new Seat(seatId, 1L, 1, SeatStatus.RESERVED, price, LocalDateTime.now().plusMinutes(5));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(memberRepository.findByIdWithLock(memberId)).thenReturn(Optional.of(member));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // when
        confirmReservationInteractor.confirm(new ConfirmReservationUseCase.Command(reservationId, memberId));

        // then
        assertThat(member.getPoints()).isEqualTo(5000L);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD);

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("잔액 부족 시 에러 발생")
    void confirm_fail_not_enough_points() {
        // given
        Long reservationId = 1L;
        Long memberId = 1L;
        Long seatId = 1L;

        Reservation reservation = new Reservation(reservationId, memberId, seatId, ReservationStatus.PENDING, LocalDateTime.now());
        Member member = new Member(memberId, 3000L); // 5000원보다 적음
        Seat seat = new Seat(seatId, 1L, 1, SeatStatus.RESERVED, 5000L, LocalDateTime.now().plusMinutes(5));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(memberRepository.findByIdWithLock(memberId)).thenReturn(Optional.of(member));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // when & then
        assertThatThrownBy(() -> confirmReservationInteractor.confirm(new ConfirmReservationUseCase.Command(reservationId, memberId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("잔액이 부족합니다.");
    }
}

