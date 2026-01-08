package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.domain.concert.Seat;

public class SeatMapper {
    public static Seat toDomain(SeatEntity entity) {
        return new Seat(entity.getId(), entity.getConcertId(), entity.getSeatNo(), entity.getStatus(), entity.getPrice());
    }

    public static SeatEntity toEntity(Seat domain) {
        SeatEntity entity = new SeatEntity();
        entity.setId(domain.getId());
        entity.setConcertId(domain.getConcertId());
        entity.setSeatNo(domain.getSeatNo());
        entity.setStatus(domain.getStatus());
        entity.setPrice(domain.getPrice());
        return entity;
    }
}
