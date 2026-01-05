package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.MyBalanceOutput
import kr.hhplus.be.server.domain.model.balance.MemberBalance
import kr.hhplus.be.server.domain.model.balance.RequestAmount
import kr.hhplus.be.server.infrastructure.persistence.balance.MemberBalanceJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.balance.mapper.MemberBalanceJpaEntityMapper
import kr.hhplus.be.server.infrastructure.persistence.member.mapper.MemberJpaEntityMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Component
class MyBalancePersistenceAdapter(
    private val repository: MemberBalanceJpaRepository,
) : MyBalanceOutput {
    @Transactional(readOnly = true)
    override fun findByMemberId(memberId: Long): Optional<MemberBalance> =
        repository
            .findByMember_Id(memberId)
            .map {
                MemberBalanceJpaEntityMapper.toDomain(
                    entity = it,
                    memberEntityToDomain = MemberJpaEntityMapper::toDomain,
                )
            }

    @Transactional(readOnly = true)
    override fun findByIdAndMemberId(
        memberBalanceId: Long,
        memberId: Long,
    ): Optional<MemberBalance> =
        repository
            .findByIdAndMember_Id(memberBalanceId, memberId)
            .map {
                MemberBalanceJpaEntityMapper.toDomain(
                    entity = it,
                    memberEntityToDomain = MemberJpaEntityMapper::toDomain,
                )
            }

    @Transactional
    override fun atomicRecharge(
        memberBalanceId: Long,
        amount: RequestAmount,
    ): Boolean = repository.rechargeBalance(memberBalanceId, amount.value) > 0

    @Transactional
    override fun atomicReduceByMemberId(
        memberId: Long,
        amount: RequestAmount,
    ): Boolean = repository.reduceBalanceByMemberId(memberId, amount.value) > 0

    @Transactional
    override fun save(myBalance: MemberBalance): MemberBalance {
        val entity =
            MemberBalanceJpaEntityMapper
                .toEntity(
                    domain = myBalance,
                    memberDomainToEntity = MemberJpaEntityMapper::toEntity,
                ).let(repository::save)

        return MemberBalanceJpaEntityMapper.toDomain(
            entity = entity,
            memberEntityToDomain = MemberJpaEntityMapper::toDomain,
        )
    }
}
