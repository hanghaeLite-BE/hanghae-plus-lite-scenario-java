package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.balance.MemberBalance
import java.util.*

interface MyBalanceOutput {
    fun findByMemberId(memberId: Long): Optional<MemberBalance>

    fun findByIdAndMemberId(
        memberBalanceId: Long,
        memberId: Long,
    ): Optional<MemberBalance>

    fun save(myBalance: MemberBalance): MemberBalance
}
