package kr.hhplus.be.server.application.service;

import kr.hhplus.be.server.domain.model.SeatReservation;
import kr.hhplus.be.server.domain.repository.SeatReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatExpirationService {

    private final SeatReservationRepository seatReservationRepository;

    public SeatExpirationService(SeatReservationRepository seatReservationRepository) {
        this.seatReservationRepository = seatReservationRepository;
    }

    @Scheduled(fixedDelay = 30000) // 30초마다 실행
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();

        // 만료된 예약 조회
        List<SeatReservation> expiredReservations = seatReservationRepository
                .findExpiredReservations(now);

        for (SeatReservation reservation : expiredReservations) {
            if (reservation.isExpired()) {
                // 상태 전환: RESERVED → AVAILABLE
                SeatReservation availableSeat = SeatReservation.createAvailableSeat(
                        reservation.getConcertId(),
                        reservation.getSeatNumber(),
                        reservation.getPrice()
                );
                availableSeat.assignId(reservation.getId());

                seatReservationRepository.save(availableSeat);
            }
        }
    }
}
