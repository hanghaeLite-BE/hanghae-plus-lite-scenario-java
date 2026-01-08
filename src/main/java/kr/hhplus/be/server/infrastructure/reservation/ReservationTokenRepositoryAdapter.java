package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.application.reservation.ReservationTokenRepositoryPort;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ReservationTokenRepositoryAdapter implements ReservationTokenRepositoryPort {

    private final ReservationTokenJpaRepository reservationTokenJpaRepository;

    public ReservationTokenRepositoryAdapter(ReservationTokenJpaRepository reservationTokenJpaRepository) {
        this.reservationTokenJpaRepository = reservationTokenJpaRepository;
    }

    @Override
    public Optional<ReservationToken> findByToken(String token) {
        return reservationTokenJpaRepository.findByToken(token).map(ReservationTokenMapper::toDomain);
    }

    @Override
    public ReservationToken save(ReservationToken token) {
        ReservationTokenEntity saved = reservationTokenJpaRepository.save(ReservationTokenMapper.toEntity(token));
        return ReservationTokenMapper.toDomain(saved);
    }
}
