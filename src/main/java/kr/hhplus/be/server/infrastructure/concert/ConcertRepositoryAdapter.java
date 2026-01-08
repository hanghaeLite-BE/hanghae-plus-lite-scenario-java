package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.application.concert.ConcertRepositoryPort;
import kr.hhplus.be.server.domain.concert.Concert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ConcertRepositoryAdapter implements ConcertRepositoryPort {
    private final ConcertJpaRepository concertJpaRepository;

    public ConcertRepositoryAdapter(ConcertJpaRepository concertJpaRepository) {
        this.concertJpaRepository = concertJpaRepository;
    }

    @Override
    public List<Concert> findAll() {
        return concertJpaRepository.findAll().stream()
                .map(ConcertMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Concert> findById(Long id) {
        return concertJpaRepository.findById(id).map(ConcertMapper::toDomain);
    }
}
