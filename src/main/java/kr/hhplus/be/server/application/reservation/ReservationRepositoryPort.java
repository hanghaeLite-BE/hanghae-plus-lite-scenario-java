package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.reservation.Reservation;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

public interface ReservationRepositoryPort {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
    List<Reservation> findAll();
}
