package kr.hhplus.be.server.interfaces.web.dto;

import kr.hhplus.be.server.domain.model.User;

public record UserBalanceResponse(
        Long id,
        String userId,
        Long balance
) {
    public static UserBalanceResponse from(User user) {
        return new UserBalanceResponse(
                user.getId(),
                user.getUserId(),
                user.getBalance()
        );
    }
}
