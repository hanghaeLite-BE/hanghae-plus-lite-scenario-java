package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryAdapter implements SeatRepositoryPort {

    private final SeatJpaRepository seatJpaRepository;
    private final SeatMapper seatMapper;

    @Override
    public List<Seat> findByConcertId(Long concertId) {
        return seatJpaRepository.findByConcertId(concertId).stream()
                .map(seatMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Seat> findById(Long id) {
        return seatJpaRepository.findById(id)
                .map(seatMapper::toDomain);
    }

    @Override
    public Seat save(Seat seat) {
        // 아쉬운 점: save 할 때마다 매퍼를 통해 변환하면서 영속성 컨텍스트를 활용하지 못함
        SeatEntity entity = seatMapper.toEntity(seat);
        return seatMapper.toDomain(seatJpaRepository.save(entity));
    }
}
