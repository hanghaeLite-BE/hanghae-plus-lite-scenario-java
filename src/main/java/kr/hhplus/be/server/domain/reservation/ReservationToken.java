package kr.hhplus.be.server.domain.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReservationToken {
    private final Long id;
    private final String token;
    private final Long userId;
    private final Long concertId;
    private TokenStatus status;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    public boolean isValid() {
        return status == TokenStatus.ISSUED && expiresAt.isAfter(LocalDateTime.now());
    }

    public void use() {
        if (!isValid()) {
            throw new IllegalStateException("유효하지 않은 토큰입니다.");
        }
        this.status = TokenStatus.USED;
    }
}
