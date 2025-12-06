package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.Reservation;
import kr.hhplus.be.server.domain.port.out.ReservationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
class ReservationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "concert_id")
    private Long concertId;

    @Column(name = "seat_number")
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    private Reservation.ReservationStatus status;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private Long price;

    // 기본 생성자, getters, setters
    protected ReservationEntity() {}

    // getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public Reservation.ReservationStatus getStatus() { return status; }
    public void setStatus(Reservation.ReservationStatus status) { this.status = status; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
}

interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, Long> {
    ReservationEntity findByUserIdAndConcertIdAndSeatNumber(Long userId, Long concertId, int seatNumber);
}

@Component
class ReservationJpaRepositoryImpl implements ReservationRepository {

    private final SpringDataReservationRepository jpaRepo;

    public ReservationJpaRepositoryImpl(SpringDataReservationRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = toEntity(reservation);
        ReservationEntity saved = jpaRepo.save(entity);
        reservation.assignId(saved.getId());
        return reservation;
    }

    @Override
    public Reservation findById(Long id) {
        ReservationEntity entity = jpaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        return toDomain(entity);
    }

    @Override
    public Reservation findByUserIdAndConcertIdAndSeatNumber(Long userId, Long concertId, int seatNumber) {
        ReservationEntity entity = jpaRepo.findByUserIdAndConcertIdAndSeatNumber(userId, concertId, seatNumber);
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
        entity.setStatus(reservation.getStatus());
        entity.setReservedAt(reservation.getReservedAt());
        entity.setExpiresAt(reservation.getExpiresAt());
        entity.setPrice(reservation.getPrice());
        return entity;
    }

    private Reservation toDomain(ReservationEntity entity) {
        Reservation reservation = Reservation.create(
                entity.getUserId(), entity.getConcertId(),
                entity.getSeatNumber(), entity.getPrice()
        );
        reservation.assignId(entity.getId());
        return reservation;
    }
}
