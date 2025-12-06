package kr.hhplus.be.server.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataSeatRepository extends JpaRepository<SeatEntity, Long> {

    SeatEntity findByConcertIdAndSeatNumber(Long concertId, int seatNumber);

    @Query("SELECT COUNT(s) > 0 FROM SeatEntity s WHERE s.concertId = :concertId AND s.seatNumber = :seatNumber AND s.status = :status")
    boolean existsByConcertIdAndSeatNumberAndStatus(
            @Param("concertId") Long concertId,
            @Param("seatNumber") int seatNumber,
            @Param("status") SeatEntity.SeatStatus status
    );
}


