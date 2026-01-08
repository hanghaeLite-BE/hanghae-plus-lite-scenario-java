package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.application.reservation.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.stereotype.Repository;

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
        ReservationEntity savedEntity = reservationJpaRepository.save(entity);
        return ReservationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id).map(ReservationMapper::toDomain);
    }
}
