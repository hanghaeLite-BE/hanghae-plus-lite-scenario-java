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
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.pointBalance += amount;
    }

    public void usePoints(Long amount) {
        if (this.pointBalance < amount) {
            throw new IllegalStateException("포인트 잔액이 부족합니다.");
        }
        this.pointBalance -= amount;
    }

    public Long getId() { return id; }
    public Long getPointBalance() { return pointBalance; }
}
