package kr.hhplus.be.server.infrastructure.persistence.member.mapper

import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.infrastructure.persistence.member.MemberJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.member.MemberType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class MemberJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 MemberJpaEntity를 Member로 변환한다")
    fun toDomain() {
        val entity = MemberJpaEntity(
            email = "test@email.com",
            pwd = "pw",
            memberType = MemberType.GENERAL
        ).apply { id = 1L }

        val domain = MemberJpaEntityMapper.toDomain(entity)

        assertEquals(entity.id, domain.id)
        assertEquals(entity.email, domain.email)
        assertEquals(entity.pwd, domain.pwd)
    }

    @Test
    @DisplayName("toEntity는 Member를 MemberJpaEntity로 변환한다")
    fun toEntity() {
        val domain = Member(
            id = 2L,
            email = "user@email.com",
            pwd = "pw2",
            memberType = kr.hhplus.be.server.domain.model.member.MemberType.GENERAL
        )

        val entity = MemberJpaEntityMapper.toEntity(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.email, entity.email)
        assertEquals(domain.pwd, entity.pwd)
        assertEquals(domain.memberType.name, entity.memberType.name)
    }
}

