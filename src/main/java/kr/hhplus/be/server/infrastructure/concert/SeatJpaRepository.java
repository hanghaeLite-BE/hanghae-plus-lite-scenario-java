package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.domain.concert.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {
    
    @Modifying
    @Query("UPDATE SeatEntity s SET s.status = :newStatus, s.reservedUntil = :reservedUntil " +
           "WHERE s.id = :seatId AND s.status = 'AVAILABLE'") // 의도적 누락: reservedUntil < :now 조건을 빼서 만료 좌석 재예약 불가 버그 유도
    int reserveSeatAtomically(@Param("seatId") Long seatId, 
                              @Param("newStatus") SeatStatus newStatus, 
                              @Param("reservedUntil") LocalDateTime reservedUntil);
}
