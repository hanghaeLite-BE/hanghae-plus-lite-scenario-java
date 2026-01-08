package kr.hhplus.be.server.application.member;

public interface ChargePointUseCase {
    void execute(Long memberId, Long amount);
}
