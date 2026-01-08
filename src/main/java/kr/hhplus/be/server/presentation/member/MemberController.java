package kr.hhplus.be.server.presentation.member;

import kr.hhplus.be.server.application.member.ChargePointUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final ChargePointUseCase chargePointUseCase;

    public MemberController(ChargePointUseCase chargePointUseCase) {
        this.chargePointUseCase = chargePointUseCase;
    }

    @PostMapping("/{memberId}/points/charge")
    public void charge(@PathVariable Long memberId, @RequestBody ChargeRequest request) {
        chargePointUseCase.execute(memberId, request.getAmount());
    }

    public static class ChargeRequest {
        private Long amount;
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
    }
}
