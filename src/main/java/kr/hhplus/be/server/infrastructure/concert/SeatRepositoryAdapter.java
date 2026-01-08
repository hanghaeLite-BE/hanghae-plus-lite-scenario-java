package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryAdapter implements SeatRepositoryPort {
    private final SeatJpaRepository seatJpaRepository;

    @Override
    public List<Seat> findAvailableSeatsByConcertDateId(Long concertDateId) {
        // SeatEntity에 concertDateId가 추가되어야 함 (기존 코드 구조에 따라 수정 필요할 수 있음)
        // 일단 SeatEntity의 concertId를 기준으로 검색하도록 구현 (예시용)
        return seatJpaRepository.findAll().stream()
                .filter(s -> s.getConcertId().equals(concertDateId))
                .map(SeatMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Seat> findById(Long id) {
        return seatJpaRepository.findById(id).map(SeatMapper::toDomain);
    }

    @Override
    public Seat save(Seat seat) {
        return SeatMapper.toDomain(seatJpaRepository.save(SeatMapper.toEntity(seat)));
    }

    public boolean reserveAtomically(Long seatId, LocalDateTime reservedUntil) {
        int updatedCount = seatJpaRepository.reserveSeatAtomically(
                seatId, 
                SeatStatus.RESERVED, 
                reservedUntil, 
                LocalDateTime.now()
        );
        return updatedCount == 1;
    }
}
