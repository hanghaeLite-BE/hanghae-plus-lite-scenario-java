package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.application.reservation.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.reservation.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationMapper reservationMapper;

    @Override
    public Reservation save(Reservation reservation) {
        // 아쉬운 점: 도메인 엔티티를 바로 저장하지 못하고 매퍼를 쓰지만, 
        // 사실상 엔티티 객체의 생명주기를 JPA에 의존하게 됨
        ReservationEntity entity = reservationMapper.toEntity(reservation);
        return reservationMapper.toDomain(reservationJpaRepository.save(entity));
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id)
                .map(reservationMapper::toDomain);
    }
}
