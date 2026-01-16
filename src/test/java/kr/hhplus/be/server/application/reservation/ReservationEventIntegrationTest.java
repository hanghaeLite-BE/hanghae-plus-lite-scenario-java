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
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

/**
 * STEP 8: Application Event 기반 예약 확정 통합 테스트 (개선된 버전)
 * 
 * 이 테스트 클래스는 "좋은 사례"를 보여주는 것을 목표로 한다.
 * 
 * 검증 항목:
 * 1. @TransactionalEventListener(phase = AFTER_COMMIT)가 제대로 동작
 * 2. 트랜잭션 커밋 후에만 플랫폼 호출이 발생
 * 3. 트랜잭션 롤백 시 플랫폼 호출이 발생하지 않음
 * 4. 플랫폼 호출 실패가 도메인 트랜잭션에 영향을 주지 않음
 */
public class ReservationEventIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    private ConfirmReservationUseCase confirmReservationUseCase;

    @SpyBean
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
    @DisplayName("예약 확정 성공 시 AFTER_COMMIT 이후 데이터 플랫폼 호출")
    void confirm_reservation_calls_platform_after_commit() {
        // given
        Long userId = 30L;
        Long seatId = 30L;
        Long concertId = 1L;
        
        // 1. 유저 생성 및 포인트 충전
        Member member = Member.builder()
                .id(userId)
                .points(10000L)
                .build();
        memberRepository.save(member);

        // 2. 토큰 발급
        ReservationToken token = ReservationToken.builder()
                .token("test-token-30")
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
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId, seatId, "test-token-30");
        Reservation reservation = reserveSeatUseCase.reserve(reserveCommand);

        // Step 2: 결제 및 예약 확정
        ConfirmReservationUseCase.Command confirmCommand = new ConfirmReservationUseCase.Command(reservation.getId(), userId);
        confirmReservationUseCase.confirm(confirmCommand);

        // then
        // 1. 도메인 로직 검증: 좌석 상태 확인
        Seat updatedSeat = seatRepository.findById(seatId).get();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.SOLD);

        // 2. 도메인 로직 검증: 예약 상태 확인
        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).get();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        // 3. 도메인 로직 검증: 유저 잔액 확인
        Member updatedMember = memberRepository.findById(userId).get();
        assertThat(updatedMember.getPoints()).isEqualTo(5000L);

        // [핵심] 4. 트랜잭션 커밋 후 플랫폼 호출이 1회 발생
        // @TransactionalEventListener(phase = AFTER_COMMIT) 덕분에
        // 트랜잭션 커밋 이후에 리스너가 실행되고, 플랫폼을 호출함
        verify(dataPlatformClient, times(1)).postReservationEvent(any(DataPlatformClient.ReservationEventPayload.class));
    }

    @Test
    @DisplayName("포인트 부족으로 인한 롤백 시 플랫폼 호출이 발생하지 않음")
    void rollback_due_to_insufficient_points_prevents_platform_call() {
        // given
        Long userId = 31L;
        Long seatId = 31L;
        Long concertId = 1L;
        
        // 1. 유저 생성: 포인트 부족 (좌석 가격 5000원보다 적음)
        Member member = Member.builder()
                .id(userId)
                .points(3000L)  // 부족!
                .build();
        memberRepository.save(member);

        // 2. 토큰 발급
        ReservationToken token = ReservationToken.builder()
                .token("test-token-31")
                .userId(userId)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        tokenRepository.save(token);

        // 3. 좌석 생성
        Seat seat = new Seat(seatId, concertId, 1, SeatStatus.AVAILABLE, 5000L, null);
        seatRepository.save(seat);

        // when & then
        // Step 1: 좌석 예약 (성공)
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId, seatId, "test-token-31");
        Reservation reservation = reserveSeatUseCase.reserve(reserveCommand);

        // Step 2: 결제 확정 (포인트 부족으로 롤백)
        ConfirmReservationUseCase.Command confirmCommand = new ConfirmReservationUseCase.Command(reservation.getId(), userId);
        
        assertThatThrownBy(() -> confirmReservationUseCase.confirm(confirmCommand))
                .isInstanceOf(IllegalArgumentException.class);

        // [핵심] 3. 트랜잭션 롤백으로 인해 @TransactionalEventListener가 실행되지 않음
        // 따라서 플랫폼 호출이 0회 (발생하지 않음)
        verify(dataPlatformClient, times(0)).postReservationEvent(any(DataPlatformClient.ReservationEventPayload.class));

        // 4. 롤백 검증: 예약 상태는 여전히 PENDING
        Reservation rollbackReservation = reservationRepository.findById(reservation.getId()).get();
        assertThat(rollbackReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);

        // 5. 롤백 검증: 좌석 상태는 여전히 RESERVED
        Seat rollbackSeat = seatRepository.findById(seatId).get();
        assertThat(rollbackSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    @DisplayName("플랫폼 호출 실패가 도메인 트랜잭션에 영향을 주지 않음")
    void platform_failure_does_not_affect_domain_transaction() {
        // given
        Long userId = 32L;
        Long seatId = 32L;
        Long concertId = 1L;
        
        // 1. 유저 생성 및 포인트 충전
        Member member = Member.builder()
                .id(userId)
                .points(10000L)
                .build();
        memberRepository.save(member);

        // 2. 토큰 발급
        ReservationToken token = ReservationToken.builder()
                .token("test-token-32")
                .userId(userId)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        tokenRepository.save(token);

        // 3. 좌석 생성
        Seat seat = new Seat(seatId, concertId, 1, SeatStatus.AVAILABLE, 5000L, null);
        seatRepository.save(seat);

        // [설정] 플랫폼 호출을 실패하도록 Mock 설정
        doThrow(new RuntimeException("데이터 플랫폼 연결 불가")).when(dataPlatformClient)
                .postReservationEvent(any(DataPlatformClient.ReservationEventPayload.class));

        // when
        // Step 1: 좌석 예약 (성공)
        ReserveSeatUseCase.Command reserveCommand = new ReserveSeatUseCase.Command(userId, seatId, "test-token-32");
        Reservation reservation = reserveSeatUseCase.reserve(reserveCommand);

        // Step 2: 결제 확정 (성공하지만, 이벤트 리스너에서 플랫폼 호출 실패)
        ConfirmReservationUseCase.Command confirmCommand = new ConfirmReservationUseCase.Command(reservation.getId(), userId);
        
        // [개선점] confirm() 메서드 자체는 예외를 던지지 않음!
        // 왜냐하면 플랫폼 호출은 AFTER_COMMIT 이후의 리스너에서 발생하기 때문
        // 도메인 트랜잭션은 이미 커밋됨
        assertThatCode(() -> confirmReservationUseCase.confirm(confirmCommand))
                .doesNotThrowAnyException();

        // then
        // [핵심] 1. 도메인 트랜잭션은 완벽하게 커밋됨 (롤백되지 않음)
        Reservation completedReservation = reservationRepository.findById(reservation.getId()).get();
        assertThat(completedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        // [핵심] 2. 좌석 상태도 커밋됨
        Seat soldSeat = seatRepository.findById(seatId).get();
        assertThat(soldSeat.getStatus()).isEqualTo(SeatStatus.SOLD);

        // [핵심] 3. 포인트도 차감됨 (커밋됨)
        Member completedMember = memberRepository.findById(userId).get();
        assertThat(completedMember.getPoints()).isEqualTo(5000L);

        // [핵심] 4. 플랫폼 호출은 시도됐지만 실패 (로그에 기록됨)
        verify(dataPlatformClient, times(1)).postReservationEvent(any(DataPlatformClient.ReservationEventPayload.class));

        // 이 상황은 "좋은 사례"의 특징:
        // - 예약 확정(핵심 비즈니스) = 완료 ✓
        // - 플랫폼 전송(부가 기능) = 실패 (로그로 추적 가능)
        // - 데이터 정합성 = 보장됨 (도메인 DB는 최신 상태)
    }
}
