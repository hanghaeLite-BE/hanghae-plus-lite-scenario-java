package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import java.util.List;
import java.util.Optional;

public interface ConcertRepositoryPort {
    List<Concert> findAll();
    Optional<Concert> findById(Long id);
}
