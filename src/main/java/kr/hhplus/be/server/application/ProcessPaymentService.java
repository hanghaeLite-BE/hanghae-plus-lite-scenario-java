package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.port.in.ProcessPaymentUseCase;
import kr.hhplus.be.server.domain.port.out.*;
import org.springframework.transaction.annotation.Transactional;

public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final PaymentGateway paymentGateway;

    public ProcessPaymentService(ReservationRepository reservationRepository,
                                 UserRepository userRepository,
                                 SeatRepository seatRepository,
                                 PaymentGateway paymentGateway) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    @Transactional
    public Payment processPayment(ProcessPaymentCommand command) {
        // 1. 예약 조회 및 유효성 검증
        Reservation reservation = reservationRepository.findById(command.getReservationId());

        if (reservation.isExpired()) {
            throw new IllegalStateException("예약이 만료되었습니다.");
        }

        // 2. 사용자 잔액 확인 및 차감
        User user = userRepository.findById(command.getUserId());

        if (!user.hasEnoughBalance(command.getAmount())) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        User updatedUser = user.deductBalance(command.getAmount());
        userRepository.save(updatedUser);

        // 3. 결제 처리
        Payment payment = Payment.create(
                command.getReservationId(),
                command.getUserId(),
                command.getAmount()
        );

        Payment completedPayment = paymentGateway.processPayment(payment);

        // 4. 예약 상태 변경 (확정)
        Reservation confirmedReservation = reservation.confirmPayment();
        reservationRepository.save(confirmedReservation);

        // 5. 좌석 상태 변경 (판매 완료)
        Seat seat = seatRepository.findByConcertIdAndSeatNumber(
                reservation.getConcertId(), reservation.getSeatNumber());
        Seat soldSeat = seat.markAsSold();
        seatRepository.save(soldSeat);

        return completedPayment;
    }
}