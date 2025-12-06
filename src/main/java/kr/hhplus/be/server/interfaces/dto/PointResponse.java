package kr.hhplus.be.server.interfaces.dto;

import kr.hhplus.be.server.domain.entity.Point;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PointResponse {

    private final Long id;
    private final Long userId;
    private final BigDecimal balance;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private PointResponse(Long id, Long userId, BigDecimal balance,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PointResponse from(Point point) {
        return new PointResponse(
                point.getId(),
                point.getUserId(),
                point.getBalance(),
                point.getCreatedAt(),
                point.getUpdatedAt()
        );
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getBalance() { return balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
