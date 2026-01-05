package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.common.annotation.UnitTest
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.utils.StubFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class MemberBalanceTest {
    @Nested
    @DisplayName("잔액 충전")
    inner class RechargeTest {
        @DisplayName("포인트 잔액이 1000원일때 500원 충전하면 잔액이 1500원이 된다.")
        @Test
        fun rechargeAddsBalance() {
            // given
            val memberBalance = StubFactory.memberBalance(id = 1L, balance = 1000L)

            // when
            val updatedBalance = memberBalance.recharge(500L)

            // then
            assertThat(updatedBalance.balance).isEqualTo(1500L)
        }

        @DisplayName("충전 금액이 0원 이하인 경우 예외 발생")
        @Test
        fun rechargeInvalidAmountThrowsException() {
            // given
            val memberBalance = StubFactory.memberBalance(id = 2L, balance = 3000L)

            // when
            val exception = assertThrows<IllegalArgumentException> { memberBalance.recharge(0L) }

            // then
            assertThat(exception.message).isEqualTo("충전 금액은 0원보다 커야합니다.")
        }
    }

    @Nested
    @DisplayName("잔액 차감")
    inner class ReduceTest {
        @DisplayName("포인트 잔액이 1000원일때 300원 차감하면 잔액이 700원이 된다.")
        @Test
        fun reduceSubtractsBalance() {
            // given
            val memberBalance = StubFactory.memberBalance(id = 1L, balance = 1000L)

            // when
            val updatedBalance = memberBalance.reduce(300L)

            // then
            assertThat(updatedBalance.balance).isEqualTo(700L)
        }

        @DisplayName("잔액이 부족한 경우 예외 발생")
        @Test
        fun reduceInsufficientBalanceThrowsException() {
            // given
            val memberBalance = StubFactory.memberBalance(id = 3L, balance = 500L)

            // when
            val exception = assertThrows<ConflictResourceException> { memberBalance.reduce(1000L) }

            // then
            assertThat(exception.message).isEqualTo("잔고의 금액이 부족합니다.")
        }
    }
}
