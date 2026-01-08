package kr.hhplus.be.server.infrastructure.member;

import kr.hhplus.be.server.domain.member.Member;

public class MemberMapper {
    public static Member toDomain(MemberEntity entity) {
        return new Member(entity.getId(), entity.getPointBalance());
    }

    public static MemberEntity toEntity(Member domain) {
        MemberEntity entity = new MemberEntity();
        entity.setId(domain.getId());
        entity.setPointBalance(domain.getPointBalance());
        return entity;
    }
}
