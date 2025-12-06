package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.entity.Concert;
import kr.hhplus.be.server.domain.entity.ConcertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

    @Query("SELECT c FROM Concert c WHERE c.status = :status AND c.concertDate > :currentDate ORDER BY c.concertDate ASC")
    List<Concert> findAvailableConcerts(@Param("status") ConcertStatus status,
                                        @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT c FROM Concert c WHERE c.concertDate BETWEEN :startDate AND :endDate ORDER BY c.concertDate ASC")
    List<Concert> findConcertsByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Concert c WHERE c.artist = :artist AND c.concertDate > :currentDate ORDER BY c.concertDate ASC")
    List<Concert> findByArtistAndFutureDates(@Param("artist") String artist,
                                             @Param("currentDate") LocalDateTime currentDate);
}

