package kr.hhplus.be.server.infrastructure.persistence.balance

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.*

interface MemberBalanceJpaRepository : JpaRepository<MemberBalanceJpaEntity, Long> {
    fun findByMember_Id(memberId: Long): Optional<MemberBalanceJpaEntity>

    fun findByIdAndMember_Id(
        memberBalanceId: Long,
        memberId: Long,
    ): Optional<MemberBalanceJpaEntity>

    @Modifying
    @Query(
        """
        UPDATE MemberBalanceJpaEntity mb
        SET mb.balance = mb.balance + :amount
        WHERE mb.id = :id
        """,
    )
    fun rechargeBalance(id: Long, amount: Long): Int

    @Modifying
    @Query(
        """
        UPDATE MemberBalanceJpaEntity mb
        SET mb.balance = mb.balance - :amount
        WHERE mb.member.id = :memberId AND mb.balance >= :amount
        """,
    )
    fun reduceBalanceByMemberId(memberId: Long, amount: Long): Int
}
