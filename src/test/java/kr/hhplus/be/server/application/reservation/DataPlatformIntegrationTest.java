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
import kr.hhplus.be.server.infrastructure.external.DataPlatformClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

/**
 * STEP 8: 데이터 플랫폼 연동 통합 테스트
 * 
 * 이 테스트 클래스는 "아쉬운 사례"를 보여주는 것을 목표로 한다.
 * 트랜잭션 내부에서 외부 API를 동기적으로 호출하는 구조의 문제점을 드러낸다.
 */
public class DataPlatformIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    private ConfirmReservationUseCase confirmReservationUseCase;

    @MockBean
    private DataPlatformClient dataPlatformClient;

    @Autowired
    private ReservationTokenRepositoryPort tokenRepository;

    @Autowired
    private SeatRepositoryPort seatRepository;

    @Autowired
    private MemberRepositoryPort memberRepository;

    @Autowired
    private ReservationRepositoryPort reservationRepository;

    @Test
    @DisplayName("예약 확정 시 데이터 플랫폼 호출 확인")
    void confirm_reservation_calls_data_platform_test() {
        // given
        Long userId = 20L;
        Long seatId = 20L;
        Long concertId = 1L;
        
        // 1. 유저 생성 및 포인트 충전
        Member member = Member.builder()
                .id(userId)
                .points(10000L)
                .build();
        memberRepository.save(member);

        // 2. 토큰 발급
        ReservationToken token = ReservationToken.builder()
                .token("test-token-20")
                .userId(userId)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        tokenRepository.save(token);

        // 3. 좌석 생성
        Seat seat = new Seat(seatId, concertId, 1, SeatStatus.AVAILABLE, 5000L, null);
        seatRepository.save(seat);

        // when
        // Step 1: 좌석 예약
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId, seatId, "test-token-20");
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

        // [핵심 검증] 데이터 플랫폼 호출이 1번 발생했는지 확인
        verify(dataPlatformClient, times(1)).postReservationEvent(any(DataPlatformClient.ReservationEventPayload.class));
    }

    @Test
    @DisplayName("데이터 플랫폼 호출 실패 시 전체 트랜잭션 롤백 테스트")
    void data_platform_failure_causes_rollback_test() {
        // given
        Long userId = 21L;
        Long seatId = 21L;
        Long concertId = 1L;
        
        // 1. 유저 생성 및 포인트 충전
        Member member = Member.builder()
                .id(userId)
                .points(10000L)
                .build();
        memberRepository.save(member);

        // 2. 토큰 발급
        ReservationToken token = ReservationToken.builder()
                .token("test-token-21")
                .userId(userId)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        tokenRepository.save(token);

        // 3. 좌석 생성
        Seat seat = new Seat(seatId, concertId, 1, SeatStatus.AVAILABLE, 5000L, null);
        seatRepository.save(seat);

        // [문제점] 데이터 플랫폼 호출을 실패하도록 Mock 설정
        doThrow(new RuntimeException("데이터 플랫폼 호출 실패")).when(dataPlatformClient)
                .postReservationEvent(any(DataPlatformClient.ReservationEventPayload.class));

        // when & then
        // Step 1: 좌석 예약
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId, seatId, "test-token-21");
        Reservation reservation = reserveSeatUseCase.reserve(reserveCommand);

        // Step 2: 결제 및 예약 확정 시 외부 API 호출 실패로 인해 전체 롤백됨
        ConfirmReservationUseCase.Command confirmCommand = new ConfirmReservationUseCase.Command(reservation.getId(), userId);

        assertThatThrownBy(() -> confirmReservationUseCase.confirm(confirmCommand))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("데이터 플랫폼");

        // [문제점] 외부 호출 실패가 DB 트랜잭션까지 롤백시킴
        // 만약 데이터 플랫폼이 일시적으로 장애가 있다면?
        // 예약 확정 자체도 실패 → 고객 경험 저하
        
        // 현재 상태: 예약은 여전히 PENDING 상태 (롤백됨)
        Reservation rollbackReservation = reservationRepository.findById(reservation.getId()).get();
        assertThat(rollbackReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);

        // 현재 상태: 좌석은 여전히 RESERVED 상태 (롤백됨)
        Seat rollbackSeat = seatRepository.findById(seatId).get();
        assertThat(rollbackSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);

        // 현재 상태: 포인트도 여전히 10000L (롤백됨)
        Member rollbackMember = memberRepository.findById(userId).get();
        assertThat(rollbackMember.getPoints()).isEqualTo(10000L);
    }
}
