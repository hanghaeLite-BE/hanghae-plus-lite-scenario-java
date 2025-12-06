package kr.hhplus.be.server.application.service;

import kr.hhplus.be.server.domain.entity.Concert;
import kr.hhplus.be.server.domain.entity.ConcertStatus;
import kr.hhplus.be.server.domain.repository.ConcertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    /**
     * 예약 가능한 콘서트 목록 조회
     */
    public List<Concert> getAvailableConcerts() {
        return concertRepository.findAvailableConcerts(
                ConcertStatus.AVAILABLE,
                LocalDateTime.now()
        );
    }

    /**
     * 콘서트 상세 정보 조회
     */
    public Concert getConcertById(Long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("콘서트를 찾을 수 없습니다: " + concertId));
    }

    /**
     * 특정 날짜 범위의 콘서트 조회
     */
    public List<Concert> getConcertsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        return concertRepository.findConcertsByDateRange(startDate, endDate);
    }

    /**
     * 특정 아티스트의 향후 콘서트 조회
     */
    public List<Concert> getConcertsByArtist(String artist) {
        if (artist == null || artist.trim().isEmpty()) {
            throw new IllegalArgumentException("아티스트명은 필수입니다.");
        }

        return concertRepository.findByArtistAndFutureDates(artist, LocalDateTime.now());
    }

    /**
     * 모든 콘서트 조회 (관리자용)
     */
    public List<Concert> getAllConcerts() {
        return concertRepository.findAll();
    }
}

