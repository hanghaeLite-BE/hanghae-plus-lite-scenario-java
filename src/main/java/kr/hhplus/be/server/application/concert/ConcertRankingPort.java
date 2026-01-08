package kr.hhplus.be.server.application.concert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

public interface ConcertRankingPort {
    void updateSalesInfo(Long concertId, Long confirmedCount, Long totalSeats);
    List<RankingResponse> getTopRankings(int limit);

    @Getter
    class RankingResponse {
        private final Long concertId;
        private final Long score;

        public RankingResponse(Long concertId, Long score) {
            this.concertId = concertId;
            this.score = score;
        }
    }
}
