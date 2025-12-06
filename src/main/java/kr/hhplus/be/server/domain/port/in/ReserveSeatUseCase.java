package kr.hhplus.be.server.domain.port.in;

import kr.hhplus.be.server.domain.model.Reservation;

public interface ReserveSeatUseCase {
    Reservation reserve(ReserveSeatCommand command);

    class ReserveSeatCommand {
        private final Long userId;
        private final Long concertId;
        private final int seatNumber;

        public ReserveSeatCommand(Long userId, Long concertId, int seatNumber) {
            this.userId = userId;
            this.concertId = concertId;
            this.seatNumber = seatNumber;
        }

        public Long getUserId() { return userId; }
        public Long getConcertId() { return concertId; }
        public int getSeatNumber() { return seatNumber; }
    }
}
