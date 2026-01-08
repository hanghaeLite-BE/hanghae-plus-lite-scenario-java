package kr.hhplus.be.server.presentation.concert;

import kr.hhplus.be.server.application.concert.GetConcertsUseCase;
import kr.hhplus.be.server.domain.concert.Concert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/concerts")
public class ConcertController {
    private final GetConcertsUseCase getConcertsUseCase;

    public ConcertController(GetConcertsUseCase getConcertsUseCase) {
        this.getConcertsUseCase = getConcertsUseCase;
    }

    @GetMapping
    public List<Concert> getConcerts() {
        return getConcertsUseCase.findAll();
    }

    @GetMapping("/{concertId}")
    public Concert getConcert(@PathVariable Long concertId) {
        return getConcertsUseCase.findById(concertId);
    }
}
