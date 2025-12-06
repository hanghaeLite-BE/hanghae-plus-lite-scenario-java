package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.service.*;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.*;
import kr.hhplus.be.server.domain.port.out.UserRepository;
import kr.hhplus.be.server.interfaces.web.UserController;
import kr.hhplus.be.server.interfaces.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ConcertReservationFlowIntegrationTest {

    @Autowired private UserController userController;
    @Autowired private SeatReservationService seatReservationService;
    @Autowired private UserRepository userRepository;
    @Autowired private ConcertRepository concertRepository;
    @Autowired private SeatReservationRepository seatReservationRepository;
    @Autowired private QueueTokenRepository queueTokenRepository;
    @Autowired private PaymentRepository paymentRepository;

    private User testUser;
    private Concert testConcert;
    private Integer testSeatNumber;
    private Long seatPrice;

    @BeforeEach
    void setUp() {
        // 사용자 생성 및 잔액 충전
        CreateUserRequest createUserRequest = new CreateUserRequest("user123", 0L);
        UserBalanceResponse userResponse = userController.createUser(createUserRequest).getBody();
        testUser = userRepository.findById(userResponse.id()).orElseThrow();

        // 콘서트 생성
        testConcert = Concert.create("테스트 콘서트", "아티스트",
                LocalDateTime.now().plusDays(7), 50, 100000);
        testConcert = concertRepository.save(testConcert);

        testSeatNumber = 1;
        seatPrice = 100000L;

        // 예약 가능한 좌석 생성
        SeatReservation availableSeat = SeatReservation.createAvailableSeat(
                testConcert.getId(), testSeatNumber, seatPrice);
        seatReservationRepository.save(availableSeat);

        // 잔액 충전
        ChargeBalanceRequest chargeRequest = new ChargeBalanceRequest(150000L);
        userController.chargeBalance(testUser.getUserId(), chargeRequest);
    }

    @Test
    @DisplayName("완전한 예약 흐름: 토큰 발급 → 좌석 예약 → Payment 결제 → 확정")
    @Transactional
    void shouldCompleteFullReservationFlowWithPayment() {
        // Given: 초기 상태 확인
        User initialUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(initialUser.getBalance()).isEqualTo(150000L);

        // When & Then: 전체 예약 프로세스 실행
        executeCompleteReservationTransaction();

        // 최종 상태 검증
        verifyCompleteReservationState();
    }

    @Transactional
    void executeCompleteReservationTransaction() {
        // 1. 토큰 발급 및 활성화
        QueueToken queueToken = QueueToken.createWaitingToken(testUser.getId(), testConcert.getId());
        queueToken = queueTokenRepository.save(queueToken);
        QueueToken activeToken = queueToken.activate();
        queueTokenRepository.save(activeToken);

        assertThat(activeToken.isActive()).isTrue();
        assertThat(activeToken.getStatus()).isEqualTo(QueueStatus.ACTIVE);

        // 2. 좌석 상태 전환: AVAILABLE → RESERVED
        SeatReservation reservedSeat = seatReservationService.reserveSeatTemporarily(
                testConcert.getId(), testSeatNumber, testUser.getId());

        assertThat(reservedSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(reservedSeat.getUserId()).isEqualTo(testUser.getId());
        assertThat(reservedSeat.getExpiresAt()).isAfter(LocalDateTime.now());

        // 3. Payment 생성 (멱등성 키 포함)
        String idempotencyKey = "payment-" + UUID.randomUUID().toString();
        Payment payment = Payment.createWithReservation(
                reservedSeat.getId(),     // reservationId
                testUser.getId(),         // userId
                seatPrice,               // amount
                "CREDIT_CARD",           // paymentMethod
                idempotencyKey           // idempotencyKey
        );

        // Payment 저장
        payment = paymentRepository.save(payment);
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        assertThat(payment.getIdempotencyKey()).isEqualTo(idempotencyKey);

        // 4. 사용자 잔액 차감
        User currentUser = userRepository.findById(testUser.getId()).orElseThrow();
        User updatedUser = currentUser.deductBalance(seatPrice);
        userRepository.save(updatedUser);

        // 5. Payment 완료 처리
        String transactionId = "txn-" + System.currentTimeMillis();
        Payment completedPayment = payment.complete(transactionId);
        paymentRepository.save(completedPayment);

        assertThat(completedPayment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(completedPayment.getTransactionId()).isEqualTo(transactionId);

        // 6. 좌석 상태 전환: RESERVED → SOLD
        SeatReservation confirmedSeat = seatReservationService.confirmReservation(
                testConcert.getId(), testSeatNumber, testUser.getId());

        assertThat(confirmedSeat.getStatus()).isEqualTo(SeatStatus.SOLD);

        // 7. 토큰 완료 처리
        QueueToken completedToken = activeToken.complete();
        queueTokenRepository.save(completedToken);

        assertThat(completedToken.getStatus()).isEqualTo(QueueStatus.COMPLETED);
    }

    void verifyCompleteReservationState() {
        // 1. 사용자 잔액 확인
        User finalUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(finalUser.getBalance()).isEqualTo(50000L); // 150,000 - 100,000

        // 2. 좌석 상태 확인
        SeatReservation finalSeat = seatReservationRepository
                .findByConcertIdAndSeatNumber(testConcert.getId(), testSeatNumber)
                .orElseThrow();
        assertThat(finalSeat.getStatus()).isEqualTo(SeatStatus.SOLD);
        assertThat(finalSeat.getUserId()).isEqualTo(testUser.getId());

        // 3. Payment 상태 확인
        Payment finalPayment = paymentRepository
                .findByReservationId(finalSeat.getId())
                .orElseThrow();
        assertThat(finalPayment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(finalPayment.getUserId()).isEqualTo(testUser.getId());
        assertThat(finalPayment.getAmount()).isEqualTo(seatPrice);
        assertThat(finalPayment.getTransactionId()).isNotNull();

        // 4. 토큰 상태 확인
        QueueToken finalToken = queueTokenRepository
                .findByUserIdAndConcertId(testUser.getId(), testConcert.getId())
                .orElseThrow();
        assertThat(finalToken.getStatus()).isEqualTo(QueueStatus.COMPLETED);
    }

    @Test
    @DisplayName("멱등성 테스트: 같은 idempotency_key로 중복 결제 시도")
    @Transactional
    void shouldPreventDuplicatePaymentWithSameIdempotencyKey() {
        // Given: 좌석 예약 완료 상태
        QueueToken activeToken = QueueToken.createWaitingToken(testUser.getId(), testConcert.getId()).activate();
        queueTokenRepository.save(activeToken);

        SeatReservation reservedSeat = seatReservationService.reserveSeatTemporarily(
                testConcert.getId(), testSeatNumber, testUser.getId());

        String idempotencyKey = "duplicate-test-key";

        // When: 첫 번째 결제 생성
        Payment firstPayment = Payment.createWithReservation(
                reservedSeat.getId(), testUser.getId(), seatPrice,
                "CREDIT_CARD", idempotencyKey);
        firstPayment = paymentRepository.save(firstPayment);

        // Then: 같은 idempotency_key로 두 번째 결제 시도 시 실패해야 함
        assertThatThrownBy(() -> {
            Payment duplicatePayment = Payment.createWithReservation(
                    reservedSeat.getId(), testUser.getId(), seatPrice,
                    "CREDIT_CARD", idempotencyKey);
            paymentRepository.save(duplicatePayment);
        }).isInstanceOf(Exception.class); // DB UNIQUE 제약 위반

        // 첫 번째 결제만 존재하는지 확인
        Payment savedPayment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
        assertThat(savedPayment.getId()).isEqualTo(firstPayment.getId());
    }
}
