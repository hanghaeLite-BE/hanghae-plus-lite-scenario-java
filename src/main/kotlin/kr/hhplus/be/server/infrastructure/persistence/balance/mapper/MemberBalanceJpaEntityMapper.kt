package kr.hhplus.be.server.infrastructure.persistence.balance.mapper

import kr.hhplus.be.server.domain.model.balance.MemberBalance
import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.infrastructure.persistence.balance.MemberBalanceJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.member.MemberJpaEntity

object MemberBalanceJpaEntityMapper {
    fun toDomain(
        entity: MemberBalanceJpaEntity,
        memberEntityToDomain: (entity: MemberJpaEntity) -> Member
    ): MemberBalance =
        MemberBalance(
            id = entity.id,
            member = memberEntityToDomain(entity.member),
            balance = entity.balance,
        )

    fun toEntity(
        domain: MemberBalance,
        memberDomainToEntity: (domain: Member) -> MemberJpaEntity
    ): MemberBalanceJpaEntity =
        MemberBalanceJpaEntity(
            member = memberDomainToEntity(domain.member),
        ).apply {
            id = domain.id
            balance = domain.balance
        }
}

