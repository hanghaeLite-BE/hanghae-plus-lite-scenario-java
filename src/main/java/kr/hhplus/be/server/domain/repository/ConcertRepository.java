package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Concert;
import kr.hhplus.be.server.domain.model.ConcertStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConcertRepository {
    Concert save(Concert concert);
    Optional<Concert> findById(Long id);
    List<Concert> findAvailableConcerts(ConcertStatus status, LocalDateTime currentDate);
    List<Concert> findConcertsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Concert> findByArtistAndFutureDates(String artist, LocalDateTime currentDate);
}

