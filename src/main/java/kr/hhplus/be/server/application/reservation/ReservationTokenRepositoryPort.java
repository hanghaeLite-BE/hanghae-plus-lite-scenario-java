package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.reservation.ReservationToken;

import java.util.Optional;

public interface ReservationTokenRepositoryPort {
    Optional<ReservationToken> findByToken(String token);
    ReservationToken save(ReservationToken token);
}
