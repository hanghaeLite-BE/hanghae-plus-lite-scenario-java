package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.model.Reservation;
import kr.hhplus.be.server.domain.model.Seat;
import kr.hhplus.be.server.domain.port.in.ReserveSeatUseCase.ReserveSeatCommand;
import kr.hhplus.be.server.domain.port.out.ReservationRepository;
import kr.hhplus.be.server.domain.port.out.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReserveSeatServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatRepository seatRepository;

    private ReserveSeatService reserveSeatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reserveSeatService = new ReserveSeatService(reservationRepository, seatRepository);
    }

    @Test
    @DisplayName("좌석 예약 성공 - 사용 가능한 좌석을 예약할 수 있다")
    void reserveSeat_Success() {
        // Given
        Long userId = 1L;
        Long concertId = 100L;
        int seatNumber = 1;

        Seat availableSeat = Seat.available(concertId, seatNumber);
        Reservation expectedReservation = Reservation.create(userId, concertId, seatNumber, 150000L);

        when(seatRepository.findByConcertIdAndSeatNumber(concertId, seatNumber))
                .thenReturn(availableSeat);
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(expectedReservation);

        // When
        ReserveSeatCommand command = new ReserveSeatCommand(userId, concertId, seatNumber);
        Reservation result = reserveSeatService.reserve(command);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(concertId, result.getConcertId());
        assertEquals(seatNumber, result.getSeatNumber());
        assertEquals(Reservation.ReservationStatus.RESERVED, result.getStatus());

        verify(seatRepository).save(any(Seat.class));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("좌석 예약 실패 - 이미 예약된 좌석을 예약하려 하면 예외가 발생한다")
    void reserveSeat_AlreadyReserved_ThrowsException() {
        // Given
        Long userId = 1L;
        Long concertId = 100L;
        int seatNumber = 1;

        Seat reservedSeat = Seat.available(concertId, seatNumber).reserve(2L); // 다른 사용자가 예약함

        when(seatRepository.findByConcertIdAndSeatNumber(concertId, seatNumber))
                .thenReturn(reservedSeat);

        // When & Then
        ReserveSeatCommand command = new ReserveSeatCommand(userId, concertId, seatNumber);

        assertThrows(IllegalStateException.class, () -> {
            reserveSeatService.reserve(command);
        });

        verify(seatRepository, never()).save(any(Seat.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}

