package kr.hhplus.be.server.interfaces.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChargeBalanceRequest(
        @NotNull @Min(1000) Long amount
) {}