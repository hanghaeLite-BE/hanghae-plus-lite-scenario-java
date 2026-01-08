package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.application.reservation.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryAdapter(ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = ReservationMapper.toEntity(reservation);
        return ReservationMapper.toDomain(reservationJpaRepository.save(entity));
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id)
                .map(ReservationMapper::toDomain);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll().stream()
                .map(ReservationMapper::toDomain)
                .toList();
    }
}
