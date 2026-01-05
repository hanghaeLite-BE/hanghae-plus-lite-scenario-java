package kr.hhplus.be.server.infrastructure.persistence.member.mapper

import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.infrastructure.persistence.member.MemberJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.member.MemberType

object MemberJpaEntityMapper {
    fun toDomain(entity: MemberJpaEntity): Member =
        Member(
            id = entity.id,
            email = entity.email,
            pwd = entity.pwd,
        )

    fun toEntity(domain: Member): MemberJpaEntity =
        MemberJpaEntity(
            email = domain.email,
            pwd = domain.pwd,
            memberType = MemberType.from(domain.memberType.name)!!
        ).apply {
            id = domain.id
        }
}

