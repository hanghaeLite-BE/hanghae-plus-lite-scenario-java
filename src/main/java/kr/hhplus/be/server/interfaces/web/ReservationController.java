package kr.hhplus.be.server.interfaces.web;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.model.Reservation;
import kr.hhplus.be.server.domain.port.in.ProcessPaymentUseCase;
import kr.hhplus.be.server.domain.port.in.ReserveSeatUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReserveSeatUseCase reserveSeatUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;

    public ReservationController(ReserveSeatUseCase reserveSeatUseCase,
                                 ProcessPaymentUseCase processPaymentUseCase) {
        this.reserveSeatUseCase = reserveSeatUseCase;
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserveSeat(@RequestBody @Valid ReserveSeatRequest request) {
        ReserveSeatUseCase.ReserveSeatCommand command = new ReserveSeatUseCase.ReserveSeatCommand(
                request.getUserId(), request.getConcertId(), request.getSeatNumber());

        Reservation reservation = reserveSeatUseCase.reserve(command);

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @PostMapping("/{reservationId}/payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long reservationId,
            @RequestBody @Valid ProcessPaymentRequest request) {

        ProcessPaymentUseCase.ProcessPaymentCommand command = new ProcessPaymentUseCase.ProcessPaymentCommand(
                reservationId, request.getUserId(), request.getAmount());

        Payment payment = processPaymentUseCase.processPayment(command);

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    // DTO Classes
    public static class ReserveSeatRequest {
        @NotNull
        private Long userId;

        @NotNull
        private Long concertId;

        @Positive
        private int seatNumber;

        // constructors, getters, setters
        public ReserveSeatRequest() {}

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getConcertId() { return concertId; }
        public void setConcertId(Long concertId) { this.concertId = concertId; }
        public int getSeatNumber() { return seatNumber; }
        public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    }

    public static class ProcessPaymentRequest {
        @NotNull
        private Long userId;

        @NotNull
        @Positive
        private Long amount;

        // constructors, getters, setters
        public ProcessPaymentRequest() {}

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
    }

    public static class ReservationResponse {
        private Long id;
        private Long userId;
        private Long concertId;
        private int seatNumber;
        private String status;
        private LocalDateTime reservedAt;
        private LocalDateTime expiresAt;
        private Long price;

        public static ReservationResponse from(Reservation reservation) {
            ReservationResponse response = new ReservationResponse();
            response.id = reservation.getId();
            response.userId = reservation.getUserId();
            response.concertId = reservation.getConcertId();
            response.seatNumber = reservation.getSeatNumber();
            response.status = reservation.getStatus().name();
            response.reservedAt = reservation.getReservedAt();
            response.expiresAt = reservation.getExpiresAt();
            response.price = reservation.getPrice();
            return response;
        }

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getConcertId() { return concertId; }
        public void setConcertId(Long concertId) { this.concertId = concertId; }
        public int getSeatNumber() { return seatNumber; }
        public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getReservedAt() { return reservedAt; }
        public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        public Long getPrice() { return price; }
        public void setPrice(Long price) { this.price = price; }
    }

    public static class PaymentResponse {
        private Long id;
        private Long reservationId;
        private Long userId;
        private Long amount;
        private String status;
        private LocalDateTime paidAt;

        public static PaymentResponse from(Payment payment) {
            PaymentResponse response = new PaymentResponse();
            response.id = payment.getId();
            response.reservationId = payment.getReservationId();
            response.userId = payment.getUserId();
            response.amount = payment.getAmount();
            response.status = payment.getStatus().name();
            response.paidAt = payment.getPaidAt();
            return response;
        }

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getReservationId() { return reservationId; }
        public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getPaidAt() { return paidAt; }
        public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    }
}

