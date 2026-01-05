package kr.hhplus.be.server.application.interactor.balance

import kr.hhplus.be.server.application.port.out.MyBalanceOutput
import kr.hhplus.be.server.application.usecase.balance.MyBalanceUseCase
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import kr.hhplus.be.server.domain.model.balance.MemberBalance
import kr.hhplus.be.server.domain.model.balance.RequestAmount
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MyBalanceInteractor(
    private val myBalanceOutput: MyBalanceOutput,
) : MyBalanceUseCase {
    @Transactional(readOnly = true)
    override fun getMyBalance(memberId: Long): MemberBalance =
        myBalanceOutput
            .findByMemberId(memberId)
            .orElseThrow {
                NotFoundResourceException(
                    message = "잔고정보를 찾을 수 없습니다.",
                    clue = mapOf("memberId" to memberId),
                )
            }

    @Transactional
    override fun rechargeMyBalance(
        memberId: Long,
        memberBalanceId: Long,
        amount: Long,
    ): MemberBalance {
        val requestAmount = RequestAmount(amount)

        if (!myBalanceOutput.atomicRecharge(memberBalanceId, requestAmount)) {
            throw NotFoundResourceException(
                message = "잔고정보를 찾을 수 없습니다.",
                clue =
                    mapOf(
                        "memberBalanceId" to memberBalanceId,
                        "memberId" to memberId,
                    ),
            )
        }

        return myBalanceOutput
            .findByIdAndMemberId(memberBalanceId, memberId)
            .orElseThrow {
                NotFoundResourceException(
                    message = "잔고정보를 찾을 수 없습니다.",
                    clue =
                        mapOf(
                            "memberBalanceId" to memberBalanceId,
                            "memberId" to memberId,
                        ),
                )
            }
    }

    @Transactional
    override fun reduceMyBalance(
        memberId: Long,
        amount: Long,
    ): MemberBalance {
        val requestAmount = RequestAmount(amount)

        if (!myBalanceOutput.atomicReduceByMemberId(memberId, requestAmount)) {
            val balance = myBalanceOutput.findByMemberId(memberId)
            if (balance.isEmpty) {
                throw NotFoundResourceException(
                    message = "잔고정보를 찾을 수 없습니다.",
                    clue = mapOf("memberId" to memberId),
                )
            }
            throw ConflictResourceException(
                message = "잔고의 금액이 부족합니다.",
                clue =
                    mapOf(
                        "memberId" to "$memberId",
                        "현재잔액" to "${balance.get().balance}",
                        "차감금액" to "$requestAmount",
                    ),
            )
        }

        return myBalanceOutput
            .findByMemberId(memberId)
            .orElseThrow {
                NotFoundResourceException(
                    message = "잔고정보를 찾을 수 없습니다.",
                    clue = mapOf("memberId" to memberId),
                )
            }
    }
}
