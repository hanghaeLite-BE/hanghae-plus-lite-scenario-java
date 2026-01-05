package kr.hhplus.be.server.infrastructure.persistence.balance.mapper

import io.mockk.mockk
import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.model.balance.MemberBalance
import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.infrastructure.persistence.balance.MemberBalanceJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.member.MemberJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class MemberBalanceJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 MemberBalanceJpaEntity를 MemberBalance로 변환한다")
    fun toDomain() {
        val memberEntity = mockk<MemberJpaEntity>()
        val member = mockk<Member>()
        val entity = MemberBalanceJpaEntity(
            member = memberEntity
        ).apply {
            id = 1L
            balance = 10000
        }
        val memberEntityToDomain: (MemberJpaEntity) -> Member = { member }

        val domain = MemberBalanceJpaEntityMapper.toDomain(entity, memberEntityToDomain)

        assertEquals(entity.id, domain.id)
        assertEquals(member, domain.member)
        assertEquals(entity.balance, domain.balance)
    }

    @Test
    @DisplayName("toEntity는 MemberBalance를 MemberBalanceJpaEntity로 변환한다")
    fun toEntity() {
        val member = mockk<Member>()
        val memberEntity = mockk<MemberJpaEntity>()
        val domain = MemberBalance(
            id = 2L,
            member = member,
            balance = 20000
        )
        val memberDomainToEntity: (Member) -> MemberJpaEntity = { memberEntity }

        val entity = MemberBalanceJpaEntityMapper.toEntity(domain, memberDomainToEntity)

        assertEquals(domain.id, entity.id)
        assertEquals(memberEntity, entity.member)
        assertEquals(domain.balance, entity.balance)
    }
}