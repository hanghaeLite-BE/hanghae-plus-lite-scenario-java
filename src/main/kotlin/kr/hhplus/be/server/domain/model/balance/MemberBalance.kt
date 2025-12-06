package kr.hhplus.be.server.domain.model.balance

import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.model.member.Member

/**
 * 잔액 포인트
 */
data class MemberBalance(
    val id: Long?,
    val balance: Long,
    val member: Member,
) {
    /**
     * 잔액 차감
     */
    fun reduce(amount: Long): MemberBalance {
        if (balance < amount) {
            throw ConflictResourceException(
                message = "잔고의 금액이 부족합니다.",
                clue = mapOf("잔고ID" to "$id", "현재잔액" to "$balance", "차감금액" to "$amount"),
            )
        }

        return copy(
            id = id,
            balance = balance - amount,
            member = member,
        )
    }

    /**
     * 잔액 충전
     */
    fun recharge(amount: Long): MemberBalance {
        if (amount <= 0) {
            throw IllegalArgumentException("충전 금액은 0원보다 커야합니다.")
        }

        return copy(
            id = id,
            balance = balance + amount,
            member = member,
        )
    }
}
