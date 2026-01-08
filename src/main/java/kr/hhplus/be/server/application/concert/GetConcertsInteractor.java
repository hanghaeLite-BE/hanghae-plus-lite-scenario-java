package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetConcertsInteractor implements GetConcertsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetConcertsInteractor.class);
    private final ConcertRepositoryPort concertRepository;

    public GetConcertsInteractor(ConcertRepositoryPort concertRepository) {
        this.concertRepository = concertRepository;
    }

    /**
     * (4) 캐시 - 과도하게 디테일한 키 설정으로 히트율 저하
     * - key는 "concert:list:{page}:{size}:{sort}"와 같이 파라미터를 그대로 반영
     * - 미세하게 다른 파라미터 요청에도 새로운 캐시가 생성되어 메모리 낭비 및 히트율 저하 유발
     * - TTL 5분, 무효화 로직 없음
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "concert:list", key = "'page:' + #page + ':size:' + #size + ':sort:' + #sort", unless = "#result == null")
    public List<Concert> findAll(int page, int size, String sort) {
        try {
            // 실제 구현에서는 페이징 처리가 필요하지만, 샘플 재현을 위해 전체 조회로 유지
            return concertRepository.findAll();
        } catch (Exception e) {
            log.warn("Failed to get concerts from cache, falling back to database", e);
            return concertRepository.findAll();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Concert findById(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));
    }
}
