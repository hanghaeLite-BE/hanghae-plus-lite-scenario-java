package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.service.SeatExpirationService;
import kr.hhplus.be.server.application.service.SeatReservationService;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.*;
import kr.hhplus.be.server.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SeatExpirationIntegrationTest {

    @Autowired private SeatReservationService seatReservationService;
    @Autowired private SeatExpirationService seatExpirationService;
    @Autowired private UserRepository userRepository;
    @Autowired private ConcertRepository concertRepository;
    @Autowired private SeatReservationRepository seatReservationRepository;
    @Autowired private PaymentRepository paymentRepository;

    private User user1, user2;
    private Concert testConcert;
    private Integer seatNumber;
    private Long seatPrice;

    @BeforeEach
    void setUp() {
        user1 = User.create("user1", 200000L);
        user2 = User.create("user2", 200000L);
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        testConcert = Concert.create("테스트 콘서트", "아티스트",
                LocalDateTime.now().plusDays(7), 50, 100000);
        testConcert = concertRepository.save(testConcert);

        seatNumber = 1;
        seatPrice = 100000L;

        // 초기 예약 가능 좌석 생성
        SeatReservation availableSeat = SeatReservation.createAvailableSeat(
                testConcert.getId(), seatNumber, seatPrice);
        seatReservationRepository.save(availableSeat);
    }

    @Test
    @DisplayName("만료 시간 테스트: 결제 미완료 예약의 자동 만료 및 재예약")
    void shouldExpireUnpaidReservationAndAllowReBooking() throws InterruptedException {
        // Given: 사용자1이 좌석 예약 + 결제 생성 (하지만 미완료 상태)
        SeatReservation reservedSeat = seatReservationService.reserveSeatTemporarily(
                testConcert.getId(), seatNumber, user1.getId());

        // 결제 생성 (PENDING 상태로 남김)
        String idempotencyKey = "pending-payment-" + UUID.randomUUID();
        Payment pendingPayment = Payment.createWithReservation(
                reservedSeat.getId(), user1.getId(), seatPrice,
                "CREDIT_CARD", idempotencyKey);
        pendingPayment = paymentRepository.save(pendingPayment);

        // Then 1: 초기 상태 확인
        assertThat(reservedSeat.getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(pendingPayment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        assertThat(reservedSeat.getExpiresAt()).isAfter(LocalDateTime.now());

        // When: 실제 시간 대기 (테스트용으로 짧은 시간 설정)
        Thread.sleep(6000); // 6초 대기

        // 만료 처리 스케줄러 실행
        seatExpirationService.expireReservations();

        // Then 2: 예약이 만료되고 Payment도 실패 처리되었는지 확인
        Payment finalPendingPayment = pendingPayment;
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            SeatReservation currentSeat = seatReservationRepository
                    .findByConcertIdAndSeatNumber(testConcert.getId(), seatNumber)
                    .orElseThrow();

            assertThat(currentSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(currentSeat.getUserId()).isNull();

            // 만료된 결제는 실패 처리되어야 함
            Payment expiredPayment = paymentRepository.findById(finalPendingPayment.getId()).orElseThrow();
            assertThat(expiredPayment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
            assertThat(expiredPayment.getFailureReason()).contains("예약 만료");
        });

        // When 2: 사용자2가 만료된 좌석을 새로 예약
        SeatReservation newReservation = seatReservationService.reserveSeatTemporarily(
                testConcert.getId(), seatNumber, user2.getId());

        // 새로운 결제 생성 및 완료 처리
        String newIdempotencyKey = "new-payment-" + UUID.randomUUID();
        Payment newPayment = Payment.createWithReservation(
                newReservation.getId(), user2.getId(), seatPrice,
                "CREDIT_CARD", newIdempotencyKey);
        newPayment = paymentRepository.save(newPayment);

        // 잔액 차감 및 결제 완료
        User updatedUser2 = user2.deductBalance(seatPrice);
        userRepository.save(updatedUser2);

        Payment completedPayment = newPayment.complete("txn-" + System.currentTimeMillis());
        paymentRepository.save(completedPayment);

        // 좌석 확정
        SeatReservation confirmedSeat = seatReservationService.confirmReservation(
                testConcert.getId(), seatNumber, user2.getId());

        // Then 3: 사용자2의 새로운 예약이 성공했는지 확인
        assertThat(confirmedSeat.getStatus()).isEqualTo(SeatStatus.SOLD);
        assertThat(confirmedSeat.getUserId()).isEqualTo(user2.getId());
        assertThat(completedPayment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);

        // 사용자1의 잔액은 변경되지 않고, 사용자2의 잔액만 차감되었는지 확인
        User finalUser1 = userRepository.findById(user1.getId()).orElseThrow();
        User finalUser2 = userRepository.findById(user2.getId()).orElseThrow();

        assertThat(finalUser1.getBalance()).isEqualTo(200000L); // 변경 없음
        assertThat(finalUser2.getBalance()).isEqualTo(100000L); // 100,000 차감
    }
}