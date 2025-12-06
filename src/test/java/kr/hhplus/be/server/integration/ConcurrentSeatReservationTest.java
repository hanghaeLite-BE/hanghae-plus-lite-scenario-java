package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.service.SeatReservationService;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.*;
import kr.hhplus.be.server.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrentSeatReservationTest {

    @Autowired private SeatReservationService seatReservationService;
    @Autowired private UserRepository userRepository;
    @Autowired private ConcertRepository concertRepository;
    @Autowired private SeatReservationRepository seatReservationRepository;
    @Autowired private PaymentRepository paymentRepository;

    private List<User> testUsers;
    private Concert testConcert;
    private Integer seatNumber;
    private Long seatPrice;

    @BeforeEach
    void setUp() {
        // 10명의 테스트 사용자 생성 (충분한 잔액)
        testUsers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            User user = User.create("concurrentUser" + i, 500000L);
            testUsers.add(userRepository.save(user));
        }

        testConcert = Concert.create("핫한 콘서트", "인기 가수",
                LocalDateTime.now().plusDays(30), 1, 200000);
        testConcert = concertRepository.save(testConcert);

        seatNumber = 1; // 단 하나의 VIP 좌석
        seatPrice = 200000L;

        // 단 하나의 예약 가능 좌석 생성
        SeatReservation availableSeat = SeatReservation.createAvailableSeat(
                testConcert.getId(), seatNumber, seatPrice);
        seatReservationRepository.save(availableSeat);
    }

    @Test
    @DisplayName("동시성 테스트: 10명이 동시 예약+결제 시도, DB 제약으로 1명만 성공")
    void shouldAllowOnlyOneSuccessInConcurrentReservationWithPayment() throws InterruptedException {
        // Given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);

        AtomicInteger reservationSuccessCount = new AtomicInteger(0);
        AtomicInteger paymentSuccessCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        // When: 10명이 정확히 동시에 예약+결제 시도
        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            final User currentUser = testUsers.get(userIndex);

            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    startLatch.await();

                    // 1단계: 좌석 예약 시도 (동시성 제어 1차)
                    SeatReservation reservedSeat = seatReservationService.reserveSeatTemporarily(
                            testConcert.getId(), seatNumber, currentUser.getId());

                    reservationSuccessCount.incrementAndGet();
                    System.out.println("✅ User " + userIndex + " 좌석 예약 성공!");

                    // 2단계: 결제 생성 및 처리 (동시성 제어 2차)
                    String idempotencyKey = "concurrent-payment-" + userIndex + "-" + UUID.randomUUID();
                    Payment payment = Payment.createWithReservation(
                            reservedSeat.getId(), currentUser.getId(), seatPrice,
                            "CREDIT_CARD", idempotencyKey);

                    payment = paymentRepository.save(payment);

                    // 잔액 차감
                    User updatedUser = currentUser.deductBalance(seatPrice);
                    userRepository.save(updatedUser);

                    // 결제 완료
                    Payment completedPayment = payment.complete("txn-" + userIndex + "-" + System.currentTimeMillis());
                    paymentRepository.save(completedPayment);

                    // 좌석 확정
                    SeatReservation confirmedSeat = seatReservationService.confirmReservation(
                            testConcert.getId(), seatNumber, currentUser.getId());

                    paymentSuccessCount.incrementAndGet();
                    System.out.println("💳 User " + userIndex + " 결제 완료 성공!");

                } catch (IllegalStateException e) {
                    // 이미 예약된 좌석 또는 비즈니스 로직 오류
                    failureCount.incrementAndGet();
                    System.out.println("❌ User " + userIndex + " 비즈니스 로직 실패: " + e.getMessage());

                } catch (DataIntegrityViolationException e) {
                    // DB 제약 위반 (UNIQUE, FK 등)
                    exceptions.add(e);
                    failureCount.incrementAndGet();
                    System.out.println("🚫 User " + userIndex + " DB 제약 위반: " + e.getMessage());

                } catch (Exception e) {
                    // 기타 예외
                    exceptions.add(e);
                    failureCount.incrementAndGet();
                    System.out.println("💥 User " + userIndex + " 예상치 못한 오류: " + e.getMessage());

                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시 시작
        startLatch.countDown();

        // 모든 스레드 완료 대기
        boolean finished = completeLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then: 결과 검증
        assertThat(finished).isTrue();

        System.out.println("=== 동시성 + Payment 테스트 결과 ===");
        System.out.println("예약 성공: " + reservationSuccessCount.get() + "명");
        System.out.println("결제 성공: " + paymentSuccessCount.get() + "명");
        System.out.println("실패: " + failureCount.get() + "명");
        System.out.println("예외: " + exceptions.size() + "건");

        // 핵심 검증: 정확히 1명만 모든 과정을 성공
        assertThat(reservationSuccessCount.get()).isEqualTo(1);
        assertThat(paymentSuccessCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(9);
        assertThat(reservationSuccessCount.get() + failureCount.get()).isEqualTo(10);

        // DB 최종 상태 확인
        SeatReservation finalSeat = seatReservationRepository
                .findByConcertIdAndSeatNumber(testConcert.getId(), seatNumber)
                .orElseThrow();

        assertThat(finalSeat.getStatus()).isEqualTo(SeatStatus.SOLD);
        assertThat(finalSeat.getUserId()).isNotNull();

        // 성공한 결제가 정확히 1건인지 확인
        Payment finalPayment = paymentRepository
                .findByReservationId(finalSeat.getId())
                .orElseThrow();

        assertThat(finalPayment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(finalPayment.getUserId()).isEqualTo(finalSeat.getUserId());
        assertThat(finalPayment.getAmount()).isEqualTo(seatPrice);

        System.out.println("최종 성공자 ID: " + finalSeat.getUserId());
        System.out.println("결제 ID: " + finalPayment.getId());
        System.out.println("좌석 상태: " + finalSeat.getStatus());
        System.out.println("결제 상태: " + finalPayment.getStatus());
    }

    @Test
    @DisplayName("Payment 멱등성 동시성 테스트: 같은 key로 동시 결제 시도")
    void shouldPreventDuplicatePaymentInConcurrentScenario() throws InterruptedException {
        // Given: 먼저 좌석 예약 완료
        User testUser = testUsers.get(0);
        SeatReservation reservedSeat = seatReservationService.reserveSeatTemporarily(
                testConcert.getId(), seatNumber, testUser.getId());

        String sharedIdempotencyKey = "shared-key-12345";
        int threadCount = 5;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When: 같은 idempotency_key로 5번 동시 결제 시도
        for (int i = 0; i < threadCount; i++) {
            final int attemptIndex = i;

            executorService.submit(() -> {
                try {
                    startLatch.await();

                    // 같은 idempotency_key로 결제 시도
                    Payment payment = Payment.createWithReservation(
                            reservedSeat.getId(), testUser.getId(), seatPrice,
                            "CREDIT_CARD", sharedIdempotencyKey);

                    paymentRepository.save(payment);
                    successCount.incrementAndGet();

                    System.out.println("💳 Attempt " + attemptIndex + " 결제 성공!");

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("❌ Attempt " + attemptIndex + " 결제 실패: " + e.getMessage());

                } finally {
                    completeLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        completeLatch.await();
        executorService.shutdown();

        // Then: 멱등성에 의해 1번만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(4);

        // DB에도 1건만 저장되었는지 확인
        Payment savedPayment = paymentRepository
                .findByIdempotencyKey(sharedIdempotencyKey)
                .orElseThrow();

        assertThat(savedPayment.getUserId()).isEqualTo(testUser.getId());
        assertThat(savedPayment.getReservationId()).isEqualTo(reservedSeat.getId());

        System.out.println("멱등성 테스트 완료: 성공 " + successCount.get() + ", 실패 " + failureCount.get());
    }
}