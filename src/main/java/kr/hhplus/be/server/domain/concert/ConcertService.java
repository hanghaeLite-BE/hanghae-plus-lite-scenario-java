package kr.hhplus.be.server.domain.concert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public Concert getConcert(Long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 콘서트입니다."));
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeats(Long concertId) {
        return seatRepository.findByConcertId(concertId);
    }
}
