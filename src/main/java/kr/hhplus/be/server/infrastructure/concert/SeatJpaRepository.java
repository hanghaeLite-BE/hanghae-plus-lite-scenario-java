package kr.hhplus.be.server.infrastructure.concert;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {
}
