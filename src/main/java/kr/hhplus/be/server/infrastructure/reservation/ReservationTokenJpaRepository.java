package kr.hhplus.be.server.infrastructure.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReservationTokenJpaRepository extends JpaRepository<ReservationTokenEntity, Long> {
    Optional<ReservationTokenEntity> findByToken(String token);
}
