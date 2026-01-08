package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Seat;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SeatRepositoryPort {
    Optional<Seat> findById(Long id);
    void save(Seat seat);
    boolean reserveAtomically(Long seatId, LocalDateTime reservedUntil);
    int releaseExpiredSeats(LocalDateTime now);
}
