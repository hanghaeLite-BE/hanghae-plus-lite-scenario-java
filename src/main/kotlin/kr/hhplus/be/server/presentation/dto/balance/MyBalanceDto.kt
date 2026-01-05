package kr.hhplus.be.server.presentation.dto.balance

import kr.hhplus.be.server.domain.model.balance.MemberBalance

data class MyBalanceResponse(
    val balanceId: Long,
    val availableAmount: Long,
) {
    constructor(memberBalance: MemberBalance) : this(
        balanceId = memberBalance.id!!,
        availableAmount = memberBalance.balance,
    )
}

data class RechargeMyBalanceRequest(
    val chargeAmount: Long,
)

data class RechargeMyBalanceResponse(
    val balanceId: Long,
    val availableAmount: Long,
) {
    constructor(memberBalance: MemberBalance) : this(
        balanceId = memberBalance.id!!,
        availableAmount = memberBalance.balance,
    )
}
