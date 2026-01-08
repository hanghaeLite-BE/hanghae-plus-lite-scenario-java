package kr.hhplus.be.server.domain.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Payment {
    private final Long id;
    private final Long reservationId;
    private final Long userId;
    private final Long amount;
    private final LocalDateTime paidAt;
}
