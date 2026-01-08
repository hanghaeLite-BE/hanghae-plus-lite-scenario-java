package kr.hhplus.be.server.infrastructure.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long points;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPoints() { return points; }
    public void setPoints(Long points) { this.points = points; }
}
