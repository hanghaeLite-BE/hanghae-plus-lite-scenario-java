package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import java.util.List;

public interface GetConcertsUseCase {
    List<Concert> findAll();
    Concert findById(Long id);
}
