package kr.hhplus.be.server.application.concert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertRankingService {

    private final ConcertRankingPort concertRankingPort;

    public void updateSalesInfo(Long concertId, Long confirmedCount, Long totalSeats) {
        concertRankingPort.updateSalesInfo(concertId, confirmedCount, totalSeats);
    }

    public List<ConcertRankingPort.RankingResponse> getTopRankings(int limit) {
        return concertRankingPort.getTopRankings(limit);
    }
}
