package kr.hhplus.be.server.presentation.concert;

import kr.hhplus.be.server.application.concert.ConcertRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class ConcertRankingController {

    private final ConcertRankingService concertRankingService;

    @GetMapping("/concerts")
    public List<ConcertRankingService.RankingResponse> getTopRankings(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return concertRankingService.getTopRankings(limit);
    }
}
