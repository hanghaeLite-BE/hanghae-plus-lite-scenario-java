package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import kr.hhplus.be.server.domain.reservation.TokenStatus;
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

class ReserveSeatInteractorTest {

    @Mock
    private SeatRepositoryPort seatRepository;
    @Mock
    private ReservationRepositoryPort reservationRepository;
    @Mock
    private ReservationTokenRepositoryPort tokenRepository;

    @InjectMocks
    private ReserveSeatInteractor reserveSeatInteractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("좌석 예약 성공 테스트")
    void reserve_success() {
        // given
        Long memberId = 1L;
        Long seatId = 1L;
        String tokenValue = "valid-token";

        ReservationToken token = ReservationToken.builder()
                .token(tokenValue)
                .userId(memberId)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        when(seatRepository.reserveAtomically(eq(seatId), any())).thenReturn(true);
        when(reservationRepository.save(any())).thenReturn(new Reservation(1L, memberId, seatId, ReservationStatus.PENDING, LocalDateTime.now()));

        // when
        Reservation result = reserveSeatInteractor.reserve(new ReserveSeatUseCase.Command(memberId, seatId, tokenValue));

        // then
        assertThat(result).isNotNull();
        assertThat(token.getStatus()).isEqualTo(TokenStatus.USED);
        verify(reservationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("이미 예약된 좌석인 경우 에러 발생")
    void reserve_fail_already_reserved() {
        // given
        Long memberId = 1L;
        Long seatId = 1L;
        String tokenValue = "valid-token";

        ReservationToken token = ReservationToken.builder()
                .token(tokenValue)
                .userId(memberId)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));
        when(seatRepository.reserveAtomically(eq(seatId), any())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> reserveSeatInteractor.reserve(new ReserveSeatUseCase.Command(memberId, seatId, tokenValue)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 예약된 좌석입니다.");
    }
}
