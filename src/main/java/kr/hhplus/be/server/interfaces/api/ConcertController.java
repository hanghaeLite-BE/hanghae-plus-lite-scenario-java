package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.application.service.ConcertService;
import kr.hhplus.be.server.domain.entity.Concert;
import kr.hhplus.be.server.interfaces.dto.ConcertResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/concerts")
public class ConcertController {

    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    /**
     * 예약 가능한 콘서트 목록 조회
     */
    @GetMapping("/available")
    public ResponseEntity<List<ConcertResponse>> getAvailableConcerts() {
        List<Concert> concerts = concertService.getAvailableConcerts();
        List<ConcertResponse> responses = concerts.stream()
                .map(ConcertResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 콘서트 상세 정보 조회
     */
    @GetMapping("/{concertId}")
    public ResponseEntity<ConcertResponse> getConcertById(@PathVariable Long concertId) {
        Concert concert = concertService.getConcertById(concertId);
        return ResponseEntity.ok(ConcertResponse.from(concert));
    }

    /**
     * 날짜 범위로 콘서트 조회
     */
    @GetMapping("/search/date-range")
    public ResponseEntity<List<ConcertResponse>> getConcertsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Concert> concerts = concertService.getConcertsByDateRange(startDate, endDate);
        List<ConcertResponse> responses = concerts.stream()
                .map(ConcertResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 아티스트별 콘서트 조회
     */
    @GetMapping("/search/artist")
    public ResponseEntity<List<ConcertResponse>> getConcertsByArtist(
            @RequestParam String artist) {

        List<Concert> concerts = concertService.getConcertsByArtist(artist);
        List<ConcertResponse> responses = concerts.stream()
                .map(ConcertResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 전체 콘서트 조회 (관리자용)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<ConcertResponse>> getAllConcerts() {
        List<Concert> concerts = concertService.getAllConcerts();
        List<ConcertResponse> responses = concerts.stream()
                .map(ConcertResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
