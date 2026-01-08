package kr.hhplus.be.server.application.concert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

public interface ConcertRankingPort {
    void updateSalesInfo(Long concertId, Long confirmedCount, Long totalSeats);
    List<RankingResponse> getTopRankings(int limit);

    @Getter
    @AllArgsConstructor
    class RankingResponse {
        private Long concertId;
        private Long score;
    }
}
