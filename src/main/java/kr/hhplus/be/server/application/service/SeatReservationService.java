package kr.hhplus.be.server.application.service;

import kr.hhplus.be.server.domain.model.SeatReservation;
import kr.hhplus.be.server.domain.repository.SeatReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class SeatReservationService {

    private final SeatReservationRepository seatReservationRepository;

    public SeatReservationService(SeatReservationRepository seatReservationRepository) {
        this.seatReservationRepository = seatReservationRepository;
    }

    /**
     * 좌석 상태 전환: 예약 가능 → 임시 예약
     */
    @Transactional
    public SeatReservation reserveSeatTemporarily(Long concertId, Integer seatNumber, Long userId) {
        // 1. 기존 좌석 조회 (FOR UPDATE로 비관적 락)
        Optional<SeatReservation> existingSeat = seatReservationRepository
                .findByConcertIdAndSeatNumberForUpdate(concertId, seatNumber);

        if (existingSeat.isPresent()) {
            SeatReservation seat = existingSeat.get();

            // 2. 예약 가능성 검증
            if (!seat.isAvailable()) {
                throw new IllegalStateException("이미 예약된 좌석입니다: " + seatNumber);
            }

            // 3. 상태 전환: AVAILABLE → RESERVED
            SeatReservation reservedSeat = SeatReservation.createTemporaryReservation(
                    concertId, seatNumber, userId, seat.getPrice()
            );
            reservedSeat.assignId(seat.getId()); // 같은 ID 유지

            return seatReservationRepository.save(reservedSeat);
        }

        throw new IllegalArgumentException("좌석을 찾을 수 없습니다: " + seatNumber);
    }

    /**
     * 좌석 상태 전환: 임시 예약 → 확정 예약
     */
    @Transactional
    public SeatReservation confirmReservation(Long concertId, Integer seatNumber, Long userId) {
        Optional<SeatReservation> existingSeat = seatReservationRepository
                .findByConcertIdAndSeatNumberForUpdate(concertId, seatNumber);

        if (existingSeat.isPresent()) {
            SeatReservation seat = existingSeat.get();

            if (!seat.canBeConfirmed(userId)) {
                throw new IllegalStateException("결제할 수 없는 예약입니다.");
            }

            // 상태 전환: RESERVED → SOLD
            SeatReservation confirmedSeat = SeatReservation.createConfirmedReservation(
                    concertId, seatNumber, userId, seat.getPrice()
            );
            confirmedSeat.assignId(seat.getId());

            return seatReservationRepository.save(confirmedSeat);
        }

        throw new IllegalArgumentException("좌석을 찾을 수 없습니다.");
    }
}