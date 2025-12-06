package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.Reservation;
import kr.hhplus.be.server.domain.port.out.ReservationRepository;
import org.springframework.stereotype.Component;

@Component
public class ReservationRepositoryImpl implements ReservationRepository {

    private final SpringDataReservationRepository jpaRepository;

    public ReservationRepositoryImpl(SpringDataReservationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = toEntity(reservation);
        ReservationEntity saved = jpaRepository.save(entity);
        reservation.assignId(saved.getId());
        return reservation;
    }

    @Override
    public Reservation findById(Long id) {
        ReservationEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + id));
        return toDomain(entity);
    }

    @Override
    public Reservation findByUserIdAndConcertIdAndSeatNumber(Long userId, Long concertId, int seatNumber) {
        ReservationEntity entity = jpaRepository.findByUserIdAndConcertIdAndSeatNumber(userId, concertId, seatNumber);
        if (entity == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
        return toDomain(entity);
    }

    private ReservationEntity toEntity(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservation.getId());
        entity.setUserId(reservation.getUserId());
        entity.setConcertId(reservation.getConcertId());
        entity.setSeatNumber(reservation.getSeatNumber());
        entity.setStatus(ReservationEntity.ReservationStatus.valueOf(reservation.getStatus().name()));
        entity.setReservedAt(reservation.getReservedAt());
        entity.setExpiresAt(reservation.getExpiresAt());
        entity.setPrice(reservation.getPrice());
        return entity;
    }

    private Reservation toDomain(ReservationEntity entity) {
        // 도메인 객체 재구성을 위한 팩토리 메서드 사용
        Reservation reservation = Reservation.create(
                entity.getUserId(),
                entity.getConcertId(),
                entity.getSeatNumber(),
                entity.getPrice()
        );
        reservation.assignId(entity.getId());
        return reservation;
    }
}