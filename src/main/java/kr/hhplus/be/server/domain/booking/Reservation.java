package kr.hhplus.be.server.domain.booking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long seatId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime createdAt;

    public enum ReservationStatus {
        RESERVED, CONFIRMED, CANCELLED
    }

    public Reservation(Long memberId, Long seatId) {
        this.memberId = memberId;
        this.seatId = seatId;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = LocalDateTime.now();
    }
}
