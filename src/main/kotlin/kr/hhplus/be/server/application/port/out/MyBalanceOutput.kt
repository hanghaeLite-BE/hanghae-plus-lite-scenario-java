package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.balance.MemberBalance
import kr.hhplus.be.server.domain.model.balance.RequestAmount
import java.util.Optional

interface MyBalanceOutput {
    fun findByMemberId(memberId: Long): Optional<MemberBalance>

    fun findByIdAndMemberId(
        memberBalanceId: Long,
        memberId: Long,
    ): Optional<MemberBalance>

    /**
     * 잔액을 원자적으로 충전합니다. (동시성 안전)
     * @return 성공 여부 (false: 해당 ID의 잔고 없음)
     */
    fun atomicRecharge(
        memberBalanceId: Long,
        amount: RequestAmount,
    ): Boolean

    /**
     * 잔액을 원자적으로 차감합니다. (동시성 안전)
     * @return 성공 여부 (false: 잔액 부족 또는 해당 ID의 잔고 없음)
     */
    fun atomicReduceByMemberId(
        memberId: Long,
        amount: RequestAmount,
    ): Boolean

    fun save(myBalance: MemberBalance): MemberBalance
}
