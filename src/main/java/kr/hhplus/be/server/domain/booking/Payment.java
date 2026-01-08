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
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reservationId;

    private Long amount;

    private LocalDateTime paymentDate;

    public Payment(Long reservationId, Long amount) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentDate = LocalDateTime.now();
    }
}
