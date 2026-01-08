package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.application.concert.ConcertRepositoryPort;
import kr.hhplus.be.server.domain.concert.Concert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryAdapter implements ConcertRepositoryPort {

    private final ConcertJpaRepository concertJpaRepository;
    private final ConcertMapper concertMapper;

    @Override
    public List<Concert> findAll() {
        // 아쉬운 점: 매퍼를 사용하지만, 실제로는 ConcertEntity와 Concert(도메인)이 거의 1:1이라 분리의 이점이 없음
        return concertJpaRepository.findAll().stream()
                .map(concertMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Concert save(Concert concert) {
        ConcertEntity entity = concertMapper.toEntity(concert);
        return concertMapper.toDomain(concertJpaRepository.save(entity));
    }
}
