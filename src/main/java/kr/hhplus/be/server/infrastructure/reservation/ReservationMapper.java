package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.domain.reservation.Reservation;

public class ReservationMapper {
    public static Reservation toDomain(ReservationEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getMemberId(),
                entity.getSeatId(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    public static ReservationEntity toEntity(Reservation domain) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(domain.getId());
        entity.setMemberId(domain.getMemberId());
        entity.setSeatId(domain.getSeatId());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
