package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.Concert;
import kr.hhplus.be.server.domain.model.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ConcertJpaRepository implements ConcertRepository {
    private final SpringDataConcertRepository concertRepo;
    private final SpringDataConcertDateRepository concertDateRepo;

    public ConcertJpaRepository(SpringDataConcertRepository concertRepo,
                                SpringDataConcertDateRepository concertDateRepo) {
        this.concertRepo = concertRepo;
        this.concertDateRepo = concertDateRepo;
    }

    @Override
    public List<ConcertDate> findAvailableDatesByMonth(LocalDate month, int limit) {
        LocalDateTime startOfMonth = month.atStartOfDay();
        LocalDateTime endOfMonth = month.plusMonths(1).atStartOfDay().minusSeconds(1);

        List<ConcertDateEntity> entities = concertDateRepo
                .findAvailableDatesByDateRangeWithLimit(startOfMonth, endOfMonth, limit);

        return entities.stream()
                .map(this::toConcertDate)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConcertDate> findAllAvailableDates(int limit) {
        List<ConcertDateEntity> entities = concertDateRepo
                .findAvailableDatesWithLimit(LocalDateTime.now(), limit);

        return entities.stream()
                .map(this::toConcertDate)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Concert> findByConcertId(String concertId) {
        return concertRepo.findById(concertId)
                .map(this::toConcert);
    }

    @Override
    public Optional<ConcertDate> findConcertDate(String concertId, LocalDateTime concertDateTime) {
        return concertDateRepo.findByConcertIdAndConcertDateTime(concertId, concertDateTime)
                .map(this::toConcertDate);
    }

    private Concert toConcert(ConcertEntity entity) {
        List<ConcertDate> concertDates = entity.getConcertDates().stream()
                .map(this::toConcertDate)
                .collect(Collectors.toList());

        return Concert.create(
                entity.getConcertId(),
                entity.getTitle(),
                entity.getVenue(),
                entity.getTotalSeats(),
                entity.getPrice(),
                concertDates
        );
    }

    private ConcertDate toConcertDate(ConcertDateEntity entity) {
        return ConcertDate.create(
                entity.getConcertId(),
                entity.getConcertDateTime(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getTotalSeats(),
                entity.getAvailableSeats()
        );
    }
}

// Spring Data JPA Repository 인터페이스들
interface SpringDataConcertRepository extends JpaRepository<ConcertEntity, String> {
}

interface SpringDataConcertDateRepository extends JpaRepository<ConcertDateEntity, Long> {

    @Query("SELECT cd FROM ConcertDateEntity cd WHERE cd.concertDateTime >= :startDate AND cd.concertDateTime <= :endDate AND cd.availableSeats > 0 ORDER BY cd.concertDateTime LIMIT :limit")
    List<ConcertDateEntity> findAvailableDatesByDateRangeWithLimit(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit
    );

    @Query("SELECT cd FROM ConcertDateEntity cd WHERE cd.concertDateTime > :now AND cd.availableSeats > 0 ORDER BY cd.concertDateTime LIMIT :limit")
    List<ConcertDateEntity> findAvailableDatesWithLimit(
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );

    Optional<ConcertDateEntity> findByConcertIdAndConcertDateTime(String concertId, LocalDateTime concertDateTime);
}
