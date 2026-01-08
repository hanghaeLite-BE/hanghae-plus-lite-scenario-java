package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.domain.reservation.ReservationToken;

public class ReservationTokenMapper {
    public static ReservationToken toDomain(ReservationTokenEntity entity) {
        return ReservationToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .userId(entity.getUserId())
                .concertId(entity.getConcertId())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static ReservationTokenEntity toEntity(ReservationToken domain) {
        ReservationTokenEntity entity = new ReservationTokenEntity();
        entity.setId(domain.getId());
        entity.setToken(domain.getToken());
        entity.setUserId(domain.getUserId());
        entity.setConcertId(domain.getConcertId());
        entity.setStatus(domain.getStatus());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
