package kr.hhplus.be.server.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

interface SpringDataConcertRepository extends JpaRepository<ConcertEntity, Long> {

    @Query("SELECT c FROM ConcertEntity c WHERE c.status = :status AND c.concertDate > :currentDate ORDER BY c.concertDate ASC")
    List<ConcertEntity> findByStatusAndConcertDateAfter(
            @Param("status") ConcertEntity.ConcertStatus status,
            @Param("currentDate") LocalDateTime currentDate
    );

    @Query("SELECT c FROM ConcertEntity c WHERE c.concertDate BETWEEN :startDate AND :endDate ORDER BY c.concertDate ASC")
    List<ConcertEntity> findByConcertDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT c FROM ConcertEntity c WHERE c.artist = :artist AND c.concertDate > :currentDate ORDER BY c.concertDate ASC")
    List<ConcertEntity> findByArtistAndConcertDateAfter(
            @Param("artist") String artist,
            @Param("currentDate") LocalDateTime currentDate
    );
}