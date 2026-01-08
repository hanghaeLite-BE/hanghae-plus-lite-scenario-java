package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;

import java.util.List;

public interface GetConcertsUseCase {
    List<Concert> findAll(int page, int size, String sort);
    Concert findById(Long id);
}
