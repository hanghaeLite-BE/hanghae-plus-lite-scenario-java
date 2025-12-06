package kr.hhplus.be.server.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PointChargeRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "충전 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "충전 금액은 0보다 커야 합니다.")
    private BigDecimal amount;

    private String description;

    // 기본 생성자
    public PointChargeRequest() {}

    public PointChargeRequest(Long userId, BigDecimal amount, String description) {
        this.userId = userId;
        this.amount = amount;
        this.description = description;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
