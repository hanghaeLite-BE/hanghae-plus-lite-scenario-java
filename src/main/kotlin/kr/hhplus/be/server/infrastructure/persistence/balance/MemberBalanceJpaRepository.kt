package kr.hhplus.be.server.infrastructure.persistence.balance

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MemberBalanceJpaRepository : JpaRepository<MemberBalanceJpaEntity, Long> {
    fun findByMember_Id(memberId: Long): Optional<MemberBalanceJpaEntity>

    fun findByIdAndMember_Id(
        memberBalanceId: Long,
        memberId: Long,
    ): Optional<MemberBalanceJpaEntity>
}
