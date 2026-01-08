package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.application.reservation.ReservationTokenRepositoryPort;
import kr.hhplus.be.server.domain.reservation.ReservationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationTokenRepositoryAdapter implements ReservationTokenRepositoryPort {
    private final ReservationTokenJpaRepository reservationTokenJpaRepository;

    @Override
    public ReservationToken save(ReservationToken token) {
        ReservationTokenEntity entity = ReservationTokenMapper.toEntity(token);
        return ReservationTokenMapper.toDomain(reservationTokenJpaRepository.save(entity));
    }

    @Override
    public Optional<ReservationToken> findByToken(String token) {
        return reservationTokenJpaRepository.findByToken(token)
                .map(ReservationTokenMapper::toDomain);
    }
}
