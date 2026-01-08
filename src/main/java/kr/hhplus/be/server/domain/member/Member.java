package kr.hhplus.be.server.domain.member;

public class Member {
    private Long id;
    private Long pointBalance;

    public Member() {}

    public Member(Long id, Long pointBalance) {
        this.id = id;
        this.pointBalance = pointBalance;
    }

    public void charge(Long amount) {
        // 아쉬운 점: 핵심 도메인 로직에 비즈니스 예외가 아닌 일반 RuntimeException 사용
        if (amount <= 0) {
            throw new RuntimeException("invalid amount");
        }
        this.pointBalance += amount;
    }

    public void usePoints(Long amount) {
        // 아쉬운 점: 잔액 부족시 구체적인 사유 없이 실패
        if (this.pointBalance < amount) {
            throw new RuntimeException("fail");
        }
        this.pointBalance -= amount;
    }

    public Long getId() { return id; }
    public Long getPointBalance() { return pointBalance; }
}
