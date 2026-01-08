package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetConcertsInteractor implements GetConcertsUseCase {

    private final ConcertRepositoryPort concertRepository;

    public GetConcertsInteractor(ConcertRepositoryPort concertRepository) {
        this.concertRepository = concertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Concert> findAll() {
        return concertRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Concert findById(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));
    }
}
