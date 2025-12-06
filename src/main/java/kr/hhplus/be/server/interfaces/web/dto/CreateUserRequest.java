package kr.hhplus.be.server.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public record CreateUserRequest(
        @NotBlank String userId,
        @Min(0) Long initialBalance
) {}
