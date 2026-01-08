package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.domain.concert.Concert;

public class ConcertMapper {
    public static Concert toDomain(ConcertEntity entity) {
        return new Concert(entity.getId(), entity.getTitle(), entity.getDateTime());
    }

    public static ConcertEntity toEntity(Concert domain) {
        ConcertEntity entity = new ConcertEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setDateTime(domain.getDateTime());
        return entity;
    }
}
