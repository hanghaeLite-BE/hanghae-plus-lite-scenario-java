package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.port.in.ProcessPaymentUseCase.ProcessPaymentCommand;
import kr.hhplus.be.server.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProcessPaymentServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private PaymentGateway paymentGateway;

    private ProcessPaymentService processPaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processPaymentService = new ProcessPaymentService(
                reservationRepository, userRepository, seatRepository, paymentGateway);
    }

    @Test
    @DisplayName("결제 처리 성공 - 유효한 예약과 충분한 잔액으로 결제할 수 있다")
    void processPayment_Success() {
        // Given
        Long reservationId = 1L;
        Long userId = 1L;
        Long amount = 150000L;
        Long concertId = 100L;
        int seatNumber = 1;

        Reservation reservation = Reservation.create(userId, concertId, seatNumber, amount);
        User user = User.create("user123", 200000L);
        Seat reservedSeat = Seat.available(concertId, seatNumber).reserve(userId);
        Payment payment = Payment.create(reservationId, userId, amount);
        Payment completedPayment = payment.complete();

        when(reservationRepository.findById(reservationId)).thenReturn(reservation);
        when(userRepository.findById(userId)).thenReturn(user);
        when(seatRepository.findByConcertIdAndSeatNumber(concertId, seatNumber))
                .thenReturn(reservedSeat);
        when(paymentGateway.processPayment(any(Payment.class))).thenReturn(completedPayment);

        // When
        ProcessPaymentCommand command = new ProcessPaymentCommand(reservationId, userId, amount);
        Payment result = processPaymentService.processPayment(command);

        // Then
        assertNotNull(result);
        assertEquals(Payment.PaymentStatus.COMPLETED, result.getStatus());
        assertEquals(reservationId, result.getReservationId());
        assertEquals(userId, result.getUserId());
        assertEquals(amount, result.getAmount());

        verify(userRepository).save(any(User.class)); // 잔액 차감된 사용자 저장
        verify(reservationRepository).save(any(Reservation.class)); // 확정된 예약 저장
        verify(seatRepository).save(any(Seat.class)); // 판매된 좌석 저장
        verify(paymentGateway).processPayment(any(Payment.class)); // 결제 처리
    }

    @Test
    @DisplayName("결제 처리 실패 - 잔액이 부족하면 예외가 발생한다")
    void processPayment_InsufficientBalance_ThrowsException() {
        // Given
        Long reservationId = 1L;
        Long userId = 1L;
        Long amount = 150000L;
        Long concertId = 100L;
        int seatNumber = 1;

        Reservation reservation = Reservation.create(userId, concertId, seatNumber, amount);
        User user = User.create("user123", 50000L); // 잔액 부족

        when(reservationRepository.findById(reservationId)).thenReturn(reservation);
        when(userRepository.findById(userId)).thenReturn(user);

        // When & Then
        ProcessPaymentCommand command = new ProcessPaymentCommand(reservationId, userId, amount);

        assertThrows(IllegalStateException.class, () -> {
            processPaymentService.processPayment(command);
        });

        verify(paymentGateway, never()).processPayment(any(Payment.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    @DisplayName("결제 처리 실패 - 만료된 예약으로는 결제할 수 없다")
    void processPayment_ExpiredReservation_ThrowsException() {
        // Given
        Long reservationId = 1L;
        Long userId = 1L;
        Long amount = 150000L;

        // Mock으로 만료된 예약 생성
        Reservation expiredReservation = mock(Reservation.class);
        when(expiredReservation.isExpired()).thenReturn(true);

        when(reservationRepository.findById(reservationId)).thenReturn(expiredReservation);

        // When & Then
        ProcessPaymentCommand command = new ProcessPaymentCommand(reservationId, userId, amount);

        assertThrows(IllegalStateException.class, () -> {
            processPaymentService.processPayment(command);
        });

        verify(userRepository, never()).findById(any());
        verify(paymentGateway, never()).processPayment(any(Payment.class));
    }
}

