package kr.hhplus.be.server.application.usecase.balance

import kr.hhplus.be.server.domain.model.balance.MemberBalance

/**
 * 잔액 조회/충전/사용
 */
interface MyBalanceUseCase {
    fun getMyBalance(memberId: Long): MemberBalance

    fun rechargeMyBalance(
        memberId: Long,
        memberBalanceId: Long,
        amount: Long,
    ): MemberBalance

    fun reduceMyBalance(
        memberId: Long,
        amount: Long,
    ): MemberBalance
}
