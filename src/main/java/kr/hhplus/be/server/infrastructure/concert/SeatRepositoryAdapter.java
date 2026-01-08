package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class SeatRepositoryAdapter implements SeatRepositoryPort {

    private final SeatJpaRepository seatJpaRepository;

    public SeatRepositoryAdapter(SeatJpaRepository seatJpaRepository) {
        this.seatJpaRepository = seatJpaRepository;
    }

    @Override
    public Optional<Seat> findById(Long id) {
        return seatJpaRepository.findById(id).map(SeatMapper::toDomain);
    }

    @Override
    public void save(Seat seat) {
        seatJpaRepository.save(SeatMapper.toEntity(seat));
    }

    @Override
    public boolean reserveAtomically(Long seatId, LocalDateTime reservedUntil) {
        int updatedRows = seatJpaRepository.reserveSeatAtomically(
                seatId,
                SeatStatus.RESERVED,
                reservedUntil,
                LocalDateTime.now()
        );
        return updatedRows > 0;
    }

    @Override
    public int releaseExpiredSeats(LocalDateTime now) {
        return seatJpaRepository.releaseExpiredSeats(now);
    }
}
