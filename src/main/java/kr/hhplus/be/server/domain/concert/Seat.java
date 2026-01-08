package kr.hhplus.be.server.domain.concert;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long concertId;

    private Integer seatNumber;

    private Long price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    public enum SeatStatus {
        AVAILABLE, RESERVED, SOLD
    }

    public Seat(Long concertId, Integer seatNumber, Long price) {
        this.concertId = concertId;
        this.seatNumber = seatNumber;
        this.price = price;
    }
}
