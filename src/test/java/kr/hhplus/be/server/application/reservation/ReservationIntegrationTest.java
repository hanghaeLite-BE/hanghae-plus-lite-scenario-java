package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.IntegrationTestBase;
import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import kr.hhplus.be.server.domain.reservation.TokenStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    private ConfirmReservationUseCase confirmReservationUseCase;

    @Autowired
    private ReservationTokenRepositoryPort tokenRepository;

    @Autowired
    private SeatRepositoryPort seatRepository;

    @Autowired
    private MemberRepositoryPort memberRepository;

    @Autowired
    private ReservationRepositoryPort reservationRepository;

    @Test
    @DisplayName("만료 후 좌석 재예약 가능 테스트")
    void seat_re_reservation_after_expiration_test() {
        // given
        Long userId = 1L;
        Long seatId = 2L;

        // 1. 유효한 토큰 발급
        ReservationToken token = ReservationToken.builder()
                .token("token-1")
                .userId(userId)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        tokenRepository.save(token);

        // 2. 이미 예약되었으나 만료된 좌석 생성
        Seat seat = new Seat(seatId, 1L, 2, SeatStatus.RESERVED, 5000L, LocalDateTime.now().minusMinutes(1));
        seatRepository.save(seat);

        // when
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId, seatId, "token-1");
        Reservation reservation = reserveSeatUseCase.reserve(reserveCommand);

        // then
        assertThat(reservation).isNotNull();
        Seat updatedSeat = seatRepository.findById(seatId).get();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    @DisplayName("토큰 발급 -> 좌석 예약 -> 결제 완료 E2E 흐름 테스트")
    void reservation_e2e_test() {
        // given
        Long userId = 10L;
        Long seatId = 10L;
        
        // 1. 유저 생성 및 포인트 충전
        Member member = Member.builder()
                .id(userId)
                .points(10000L)
                .build();
        memberRepository.save(member);

        // 2. 토큰 발급
        ReservationToken token = ReservationToken.builder()
                .token("test-token-123")
                .userId(userId)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        tokenRepository.save(token);

        // 3. 좌석 생성
        Seat seat = new Seat(seatId, 1L, 1, SeatStatus.AVAILABLE, 5000L, null);
        seatRepository.save(seat);

        // when
        // Step 1: 좌석 예약
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId, seatId, "test-token-123");
        Reservation reservation = reserveSeatUseCase.reserve(reserveCommand);

        // Step 2: 결제 및 예약 확정
        ConfirmReservationUseCase.Command confirmCommand = new ConfirmReservationUseCase.Command(reservation.getId(), userId);
        confirmReservationUseCase.confirm(confirmCommand);

        // then
        // 1. 좌석 상태 확인
        Seat updatedSeat = seatRepository.findById(seatId).get();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.SOLD);

        // 2. 예약 상태 확인
        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).get();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        // 3. 유저 잔액 확인
        Member updatedMember = memberRepository.findById(userId).get();
        assertThat(updatedMember.getPoints()).isEqualTo(5000L);

        // 4. 토큰 상태 확인
        ReservationToken updatedToken = tokenRepository.findByToken("test-token-123").get();
        assertThat(updatedToken.getStatus()).isEqualTo(TokenStatus.USED);
    }
}
