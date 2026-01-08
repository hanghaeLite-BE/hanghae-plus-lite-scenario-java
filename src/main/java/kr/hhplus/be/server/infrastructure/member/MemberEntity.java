package kr.hhplus.be.server.infrastructure.member;

import jakarta.persistence.*;

@Entity
@Table(name = "member")
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pointBalance;

    public MemberEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPointBalance() { return pointBalance; }
    public void setPointBalance(Long pointBalance) { this.pointBalance = pointBalance; }
}
