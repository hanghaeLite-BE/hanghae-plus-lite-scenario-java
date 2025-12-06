package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.Seat;
import kr.hhplus.be.server.domain.port.out.SeatRepository;
import org.springframework.stereotype.Component;

@Component
public class SeatRepositoryImpl implements SeatRepository {

    private final SpringDataSeatRepository jpaRepository;

    public SeatRepositoryImpl(SpringDataSeatRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Seat findByConcertIdAndSeatNumber(Long concertId, int seatNumber) {
        SeatEntity entity = jpaRepository.findByConcertIdAndSeatNumber(concertId, seatNumber);
        if (entity == null) {
            throw new IllegalArgumentException("좌석을 찾을 수 없습니다. 콘서트 ID: " + concertId + ", 좌석 번호: " + seatNumber);
        }
        return toDomain(entity);
    }

    @Override
    public Seat save(Seat seat) {
        SeatEntity entity = toEntity(seat);
        SeatEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByConcertIdAndSeatNumberAndStatus(Long concertId, int seatNumber, Seat.SeatStatus status) {
        SeatEntity.SeatStatus entityStatus = SeatEntity.SeatStatus.valueOf(status.name());
        return jpaRepository.existsByConcertIdAndSeatNumberAndStatus(concertId, seatNumber, entityStatus);
    }

    private SeatEntity toEntity(Seat seat) {
        SeatEntity entity = new SeatEntity();
        entity.setConcertId(seat.getConcertId());
        entity.setSeatNumber(seat.getSeatNumber());
        entity.setStatus(SeatEntity.SeatStatus.valueOf(seat.getStatus().name()));
        entity.setReservedByUserId(seat.getReservedByUserId());
        return entity;
    }

    private Seat toDomain(SeatEntity entity) {
        if (entity.getStatus() == SeatEntity.SeatStatus.AVAILABLE) {
            return Seat.available(entity.getConcertId(), entity.getSeatNumber());
        } else {
            // 예약된 좌석의 경우 팩토리 메서드로 생성 후 상태 변경
            Seat seat = Seat.available(entity.getConcertId(), entity.getSeatNumber());
            if (entity.getStatus() == SeatEntity.SeatStatus.RESERVED && entity.getReservedByUserId() != null) {
                seat = seat.reserve(entity.getReservedByUserId());
            } else if (entity.getStatus() == SeatEntity.SeatStatus.SOLD) {
                seat = seat.reserve(entity.getReservedByUserId()).markAsSold();
            }
            return seat;
        }
    }
}
