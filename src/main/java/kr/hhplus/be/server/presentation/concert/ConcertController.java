package kr.hhplus.be.server.presentation.concert;

import kr.hhplus.be.server.application.concert.GetConcertsUseCase;
import kr.hhplus.be.server.domain.concert.Concert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concerts")
public class ConcertController {

    private final GetConcertsUseCase getConcertsUseCase;

    public ConcertController(GetConcertsUseCase getConcertsUseCase) {
        this.getConcertsUseCase = getConcertsUseCase;
    }

    @GetMapping
    public List<Concert> getConcerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id:desc") String sort
    ) {
        return getConcertsUseCase.findAll(page, size, sort);
    }

    @GetMapping("/{id}")
    public Concert getConcert(@PathVariable Long id) {
        return getConcertsUseCase.findById(id);
    }
}
