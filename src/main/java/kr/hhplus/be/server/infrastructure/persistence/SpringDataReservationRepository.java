package kr.hhplus.be.server.infrastructure.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, Long> {

    @Query("SELECT r FROM ReservationEntity r WHERE r.userId = :userId AND r.concertId = :concertId AND r.seatNumber = :seatNumber")
    ReservationEntity findByUserIdAndConcertIdAndSeatNumber(
            @Param("userId") Long userId,
            @Param("concertId") Long concertId,
            @Param("seatNumber") int seatNumber
    );
}