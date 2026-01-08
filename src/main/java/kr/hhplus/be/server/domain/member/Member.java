package kr.hhplus.be.server.domain.member;

public class Member {
    private final Long id;
    private Long points;

    public Member(Long id, Long points) {
        this.id = id;
        this.points = points;
    }

    public static MemberBuilder builder() {
        return new MemberBuilder();
    }

    public Long getId() { return id; }
    public Long getPoints() { return points; }

    public void charge(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.points += amount;
    }

    public void usePoints(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }
        if (this.points < amount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.points -= amount;
    }

    public static class MemberBuilder {
        private Long id;
        private Long points;

        public MemberBuilder id(Long id) { this.id = id; return this; }
        public MemberBuilder points(Long points) { this.points = points; return this; }

        public Member build() {
            return new Member(id, points);
        }
    }
}
