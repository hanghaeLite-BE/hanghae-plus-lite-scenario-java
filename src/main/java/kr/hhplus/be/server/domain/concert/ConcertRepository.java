package kr.hhplus.be.server.domain.concert;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
}
