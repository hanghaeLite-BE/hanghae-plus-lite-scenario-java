package kr.hhplus.be.server.infrastructure.member;

import kr.hhplus.be.server.domain.member.Member;

public class MemberMapper {
    public static Member toDomain(MemberEntity entity) {
        return Member.builder()
                .id(entity.getId())
                .points(entity.getPoints())
                .build();
    }

    public static MemberEntity toEntity(Member domain) {
        MemberEntity entity = new MemberEntity();
        entity.setId(domain.getId());
        entity.setPoints(domain.getPoints());
        return entity;
    }
}
