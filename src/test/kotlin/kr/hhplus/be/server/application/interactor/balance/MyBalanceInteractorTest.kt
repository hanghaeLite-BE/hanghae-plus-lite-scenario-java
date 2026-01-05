package kr.hhplus.be.server.application.interactor.balance

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.application.port.out.MyBalanceOutput
import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import kr.hhplus.be.server.domain.model.balance.MemberBalance
import kr.hhplus.be.server.domain.model.balance.RequestAmount
import kr.hhplus.be.server.domain.utils.StubFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

@UnitTest
class MyBalanceInteractorTest {
    @Nested
    @DisplayName("잔액 조회")
    inner class GetMyBalance {
        private val myBalanceOutput = mockk<MyBalanceOutput>()
        private val myBalanceInteractor = MyBalanceInteractor(myBalanceOutput = myBalanceOutput)

        @Test
        @DisplayName("존재하지 않는 회원의 잔액을 조회하는 경우 예외가 발생한다")
        fun getMyBalance_NotExistMember() {
            // given
            val memberId = 1L
            every { myBalanceOutput.findByMemberId(1L) } returns Optional.empty()

            val exception =
                assertThrows<NotFoundResourceException> {
                    myBalanceInteractor.getMyBalance(memberId)
                }
            assertThat(exception.message).isEqualTo("잔고정보를 찾을 수 없습니다.")
        }

        @Test
        @DisplayName("존재하는 회원의 잔액을 조회하는 경우 잔액이 반환된다")
        fun getMyBalance_ExistMember() {
            // given
            val member = StubFactory.member(id = 2L)
            val memberBalance =
                MemberBalance(
                    id = 1L,
                    member = member,
                    balance = 1000L,
                )
            every { myBalanceOutput.findByMemberId(2L) } returns Optional.of(memberBalance)

            // when
            val actualBalance = myBalanceInteractor.getMyBalance(2L)

            // then
            assertThat(actualBalance.member.id).isEqualTo(2L)
            assertThat(actualBalance.balance).isEqualTo(1000L)
        }
    }

    @Nested
    @DisplayName("잔액 충전")
    inner class RechargeMyBalance {
        private val myBalanceOutput = mockk<MyBalanceOutput>()
        private val myBalanceInteractor = MyBalanceInteractor(myBalanceOutput = myBalanceOutput)

        @Test
        @DisplayName("존재하지 않는 회원의 잔액을 충전하는 경우 예외가 발생한다")
        fun rechargeMyBalance_NotExistMember() {
            // given
            val memberId = 1L
            val memberBalanceId = 1L
            val amount = 1000L
            every { myBalanceOutput.atomicRecharge(memberBalanceId, RequestAmount(1000L)) } returns false

            // when & then
            val exception =
                assertThrows<NotFoundResourceException> {
                    myBalanceInteractor.rechargeMyBalance(memberId, memberBalanceId, amount)
                }
            assertThat(exception.message).isEqualTo("잔고정보를 찾을 수 없습니다.")
        }

        @Test
        @DisplayName("존재하는 회원의 잔액을 충전하는 경우 충전된 잔액이 반환된다")
        fun rechargeMyBalance_ExistMember() {
            // given
            val member = StubFactory.member(id = 2L)
            val rechargedBalance =
                MemberBalance(
                    id = 1L,
                    member = member,
                    balance = 2000L,
                )
            every { myBalanceOutput.atomicRecharge(1L, RequestAmount(1000L)) } returns true
            every {
                myBalanceOutput.findByIdAndMemberId(1L, 2L)
            } returns Optional.of(rechargedBalance)

            // when
            val actualBalance = myBalanceInteractor.rechargeMyBalance(2L, 1L, 1000L)

            // then
            assertThat(actualBalance.member.id).isEqualTo(2L)
            assertThat(actualBalance.balance).isEqualTo(2000L)
        }
    }
}
