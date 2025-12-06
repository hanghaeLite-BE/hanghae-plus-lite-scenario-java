package kr.hhplus.be.server.interfaces.dto;

import kr.hhplus.be.server.domain.entity.PointHistory;
import kr.hhplus.be.server.domain.entity.PointTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PointHistoryResponse {

    private final Long id;
    private final Long userId;
    private final BigDecimal amount;
    private final PointTransactionType type;
    private final String description;
    private final LocalDateTime createdAt;

    private PointHistoryResponse(Long id, Long userId, BigDecimal amount,
                                 PointTransactionType type, String description,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static PointHistoryResponse from(PointHistory history) {
        return new PointHistoryResponse(
                history.getId(),
                history.getUserId(),
                history.getAmount(),
                history.getType(),
                history.getDescription(),
                history.getCreatedAt()
        );
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public PointTransactionType getType() { return type; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
