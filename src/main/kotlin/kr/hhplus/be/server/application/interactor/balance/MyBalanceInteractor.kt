package kr.hhplus.be.server.application.interactor.balance

import kr.hhplus.be.server.application.port.out.MyBalanceOutput
import kr.hhplus.be.server.application.usecase.balance.MyBalanceUseCase
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import kr.hhplus.be.server.domain.model.balance.MemberBalance
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
        val rechargedBalance: MemberBalance =
            myBalanceOutput
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
                }.recharge(amount)

        return myBalanceOutput.save(rechargedBalance)
    }

    @Transactional
    override fun reduceMyBalance(
        memberId: Long,
        amount: Long,
    ): MemberBalance {
        val reducedBalance: MemberBalance =
            myBalanceOutput
                .findByMemberId(memberId)
                .orElseThrow {
                    // 방어코드지만 데이터의 정합성이 깨지지않는한 발생하는 경우는 없다
                    NotFoundResourceException(
                        message = "잔고정보를 찾을 수 없습니다.",
                        clue = mapOf("memberId" to memberId),
                    )
                }.reduce(amount)
        return myBalanceOutput.save(reducedBalance)
    }
}
