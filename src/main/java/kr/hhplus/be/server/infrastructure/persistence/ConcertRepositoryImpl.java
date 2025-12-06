package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.Concert;
import kr.hhplus.be.server.domain.model.ConcertStatus;
import kr.hhplus.be.server.domain.repository.ConcertRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ConcertRepositoryImpl implements ConcertRepository {

    private final SpringDataConcertRepository jpaRepository;

    public ConcertRepositoryImpl(SpringDataConcertRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Concert save(Concert concert) {
        ConcertEntity entity = toEntity(concert);
        ConcertEntity saved = jpaRepository.save(entity);
        concert.assignId(saved.getId());
        return concert;
    }

    @Override
    public Optional<Concert> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Concert> findAvailableConcerts(ConcertStatus status, LocalDateTime currentDate) {
        ConcertEntity.ConcertStatus entityStatus = ConcertEntity.ConcertStatus.valueOf(status.name());
        return jpaRepository.findByStatusAndConcertDateAfter(entityStatus, currentDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Concert> findConcertsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByConcertDateBetween(startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Concert> findByArtistAndFutureDates(String artist, LocalDateTime currentDate) {
        return jpaRepository.findByArtistAndConcertDateAfter(artist, currentDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ConcertEntity toEntity(Concert concert) {
        ConcertEntity entity = new ConcertEntity();
        entity.setId(concert.getId());
        entity.setTitle(concert.getTitle());
        entity.setArtist(concert.getArtist());
        entity.setVenue(concert.getVenue());
        entity.setConcertDate(concert.getConcertDate());
        entity.setStartTime(concert.getStartTime());
        entity.setEndTime(concert.getEndTime());
        entity.setTotalSeats(concert.getTotalSeats());
        entity.setAvailableSeats(concert.getAvailableSeats());
        entity.setPrice(concert.getPrice());
        entity.setStatus(ConcertEntity.ConcertStatus.valueOf(concert.getStatus().name()));
        return entity;
    }

    private Concert toDomain(ConcertEntity entity) {
        Concert concert = Concert.create(
                entity.getTitle(),
                entity.getArtist(),
                entity.getVenue(),
                entity.getConcertDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getTotalSeats(),
                entity.getPrice()
        );
        concert.assignId(entity.getId());
        return concert;
    }
}
