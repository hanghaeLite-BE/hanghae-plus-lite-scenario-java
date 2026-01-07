package io.hhplus.tdd.point.application;

import io.hhplus.tdd.common.LockManager;
import io.hhplus.tdd.common.exception.AmountExceedBalanceException;
import io.hhplus.tdd.common.exception.ChargePointFailureException;
import io.hhplus.tdd.common.exception.InvariantViolationException;
import io.hhplus.tdd.common.exception.UsePointFailureException;
import io.hhplus.tdd.common.pagination.PageRequest;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.infrastructure.PointHistoryRepository;
import io.hhplus.tdd.point.infrastructure.UserPointRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointRepository userPointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;

    private PointService pointService;

    @BeforeEach
    void beforeEach() {
        LockManager lockManager = new LockManager();
        pointService = new PointServiceImpl(lockManager, userPointRepository, pointHistoryRepository);
    }

    @Nested
    @DisplayName("포인트 조회 - findUserPointById()")
    class FindUserPointByIdTest {
        // 테스트 작성 이유: 정상 시나리오에서 서비스가 올바르게 동작하는지 검증하기 위해
        @Test
        @DisplayName("회원 아이디로 포인트를 조회하면 해당하는 UserPoint 객체가 반환된다.")
        void givenId_whenFindPoint_thenReturnUserPoint() {
            // given
            long id = 0L;
            when(userPointRepository.findById(id))
                    .thenReturn(new UserPoint(id, 100L, System.currentTimeMillis()));

            // when
            UserPoint userPoint = pointService.findUserPointById(id);

            // then
            assertThat(userPoint.point()).isEqualTo(100L);
        }

        // 테스트 작성 이유: DB에 존재하지 않는 아이디가 주어졌을 때, 서비스가 예외 시나리오를 정상적으로 처리하는지 검증하기 위해
        @Test
        @DisplayName("존재하지 않는 회원 아이디로 포인트를 조회하면 포인트가 0으로 초기화된 UserPoint 객체가 반환된다.")
        void givenNotExistId_whenFindPoint_thenReturnZeroPoint() {
            // given
            long id = 1L;
            when(userPointRepository.findById(id))
                    .thenReturn(UserPoint.empty(id));

            // when
            UserPoint userPoint = pointService.findUserPointById(id);

            // then
            assertThat(userPoint.point()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("포인트 내역 조회 - findPointHistoriesByUserId()")
    class FindPointHistoriesByUserIdTest {
        // 테스트 작성 이유: 정상 시나리오에서 서비스가 올바르게 동작하는지 검증하기 위해
        @Test
        @DisplayName("회원 아이디로 포인트 내역을 조회하면 해당하는 PointHistory의 리스트가 반환된다.")
        void givenId_whenFindPointHistory_thenReturnPointHistoryList() {
            // given
            long userId = 0L;
            PointHistory sampleHistory = new PointHistory(0L, userId, 1000L, TransactionType.CHARGE,
                    System.currentTimeMillis());
            PageRequest pageRequest = new PageRequest(0, 10);
            when(pointHistoryRepository.findAllByUserId(userId, pageRequest))
                    .thenReturn(List.of(sampleHistory));

            // when
            List<PointHistory> pointHistoryList = pointService.findPointHistoriesByUserId(userId, pageRequest);

            // then
            assertThat(pointHistoryList).isNotEmpty();
            assertThat(pointHistoryList.get(0)).isEqualTo(sampleHistory);
        }

        // 테스트 작성 이유: DB에 존재하지 않는 아이디가 주어졌을 때, 서비스가 예외 시나리오를 정상적으로 처리하는지 검증하기 위해
        @Test
        @DisplayName("존재하지 않는 회원 아이디로 포인트 내역을 조회하면 빈 PointHistory 리스트가 반환된다.")
        void givenNotExistId_whenFindPointHistory_thenReturnEmptyPointHistoryList() {
            // given
            long userId = 1L;
            PageRequest pageRequest = new PageRequest(0, 10);
            when(pointHistoryRepository.findAllByUserId(userId, pageRequest))
                    .thenReturn(List.of());

            // when
            List<PointHistory> pointHistoryList = pointService.findPointHistoriesByUserId(userId, pageRequest);

            // then
            assertThat(pointHistoryList).isEmpty();
        }
    }

    @Nested
    @DisplayName("포인트 충전 - chargeUserPoint()")
    class ChargeUserPointTest {
        // 테스트 작성 이유: 정상 시나리오에서 서비스가 올바르게 동작하는지 검증하기 위해
        @Test
        @DisplayName("회원 아이디와 충전 금액이 주어지면 해당 금액만큼 포인트가 증가된 UserPoint 객체가 반환된다.")
        void givenIdAndAmount_whenChargePoint_thenReturnUpdatedUserPoint() {
            // given
            long userId = 0L;
            long amount = 100L;
            UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());
            when(userPointRepository.findById(userId))
                    .thenReturn(userPoint);
            when(userPointRepository.save(any(UserPoint.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            UserPoint result = pointService.chargeUserPoint(userId, amount);

            // then
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.point()).isEqualTo(userPoint.point() + amount);

            ArgumentCaptor<PointHistory> pointHistoryCaptor = ArgumentCaptor.forClass(PointHistory.class);
            verify(pointHistoryRepository).save(pointHistoryCaptor.capture());
            assertThat(pointHistoryCaptor.getValue().userId()).isEqualTo(userId);
            assertThat(pointHistoryCaptor.getValue().amount()).isEqualTo(amount);
            assertThat(pointHistoryCaptor.getValue().type()).isEqualTo(TransactionType.CHARGE);
        }

        // 테스트 작성 이유: 부적합한 충전 금액이 주어졌을 때, 서비스가 예외 시나리오를 정상적으로 처리하는지 검증하기 위해
        // - amount < 0 : '충전'이라는 역할을 훼손하기 때문에 부적합함
        // - amount == 0 : 유효한 동작을 하지 않기 때문에 부적합함
        @Test
        @DisplayName("충전 금액이 0 이하로 주어지면 InvariantViolationException이 발생한다.")
        void givenNonPositiveAmount_whenChargePoint_thenThrowInvariantViolationException() {
            // given
            long id = 0L;
            long amountZero = 0L;
            long amountNegative = -1L;

            // when
            ThrowableAssert.ThrowingCallable caseZero = () -> pointService.chargeUserPoint(id, amountZero);
            ThrowableAssert.ThrowingCallable caseNegative = () -> pointService.chargeUserPoint(id, amountNegative);

            // then
            assertThatThrownBy(caseZero).isInstanceOf(InvariantViolationException.class);
            assertThatThrownBy(caseNegative).isInstanceOf(InvariantViolationException.class);

            verifyNoInteractions(userPointRepository, pointHistoryRepository);
        }

        // 테스트 작성 이유: 트랜잭션의 원자성을 검증하기 위해
        // 추가로 처음에는 롤백을 테스트하려고 했지만, 단위 테스트에서 롤백을 검증하는 것은 제한적이라고 생각하여 예외만 검증
        @Test
        @DisplayName("UserPoint 저장 중 DB 오류가 발생하면 ChargePointFailureException이 발생하고, 포인트 내역이 기록되지 않는다.")
        void givenUserPointSaveError_whenChargePoint_thenThrowChargePointFailureException() {
            // given
            when(userPointRepository.findById(anyLong()))
                    .thenReturn(new UserPoint(0L, 100L, System.currentTimeMillis()));
            when(userPointRepository.save(any(UserPoint.class)))
                    .thenThrow(new RuntimeException("DB error"));

            // when
            ThrowableAssert.ThrowingCallable caseUserPoint = () -> pointService.chargeUserPoint(0L, 100L);

            // then
            assertThatThrownBy(caseUserPoint).isInstanceOf(ChargePointFailureException.class);
            verify(pointHistoryRepository, never()).save(any(PointHistory.class));
        }

        // 테스트 작성 이유: 트랜잭션의 원자성을 검증하기 위해
        // 추가로 처음에는 롤백을 테스트하려고 했지만, 단위 테스트에서 롤백을 검증하는 것은 제한적이라고 생각하여 예외만 검증
        @Test
        @DisplayName("PointHistory 저장 중 DB 오류가 발생하면 ChargePointFailureException이 발생한다.")
        void givenPointHistorySaveError_whenChargePoint_thenThrowChargePointFailureException() {
            // given
            when(userPointRepository.findById(anyLong()))
                    .thenReturn(new UserPoint(0L, 100L, System.currentTimeMillis()));
            when(userPointRepository.save(any(UserPoint.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pointHistoryRepository.save(any(PointHistory.class)))
                    .thenThrow(new RuntimeException("DB error"));

            // when
            ThrowableAssert.ThrowingCallable casePointHistory = () -> pointService.chargeUserPoint(0L, 100L);

            // then
            assertThatThrownBy(casePointHistory).isInstanceOf(ChargePointFailureException.class);
        }
    }

    @Nested
    @DisplayName("포인트 사용 - useUserPoint()")
    class UseUserPointTest {
        // 테스트 작성 이유: 정상 시나리오에서 서비스가 올바르게 동작하는지 검증하기 위해
        @Test
        @DisplayName("회원 아이디와 사용 금액이 주어지면 해당 금액만큼 포인트가 차감된 UserPoint 객체가 반환된다.")
        void givenIdAndAmount_whenUsePoint_thenReturnUpdatedUserPoint() {
            // given
            long userId = 0L;
            long amount = 100L;

            when(userPointRepository.findById(userId))
                    .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));
            when(userPointRepository.save(any(UserPoint.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            UserPoint userPoint = pointService.useUserPoint(userId, amount);

            // then
            assertThat(userPoint.id()).isEqualTo(userId);
            assertThat(userPoint.point()).isEqualTo(0L);

            ArgumentCaptor<PointHistory> pointHistoryCaptor = ArgumentCaptor.forClass(PointHistory.class);
            verify(pointHistoryRepository).save(pointHistoryCaptor.capture());
            assertThat(pointHistoryCaptor.getValue().userId()).isEqualTo(userId);
            assertThat(pointHistoryCaptor.getValue().amount()).isEqualTo(amount);
            assertThat(pointHistoryCaptor.getValue().type()).isEqualTo(TransactionType.USE);
        }

        // 테스트 작성 이유: 부적합한 사용 금액이 주어졌을 때, 서비스가 예외 시나리오를 정상적으로 처리하는지 검증하기 위해
        // - amount < 0 : '사용'이라는 역할을 훼손하기 때문에 부적합함
        // - amount == 0 : 유효한 동작을 하지 않기 때문에 부적합함
        @Test
        @DisplayName("사용 금액이 0 이하로 주어지면 InvariantViolationException이 발생한다.")
        void givenNonPositiveAmount_whenUsePoint_thenThrowInvariantViolationException() {
            // given
            long userId = 0L;
            long amountZero = 0L;
            long amountNegative = -1L;

            // when
            ThrowableAssert.ThrowingCallable caseZero = () -> pointService.useUserPoint(userId, amountZero);
            ThrowableAssert.ThrowingCallable caseNegative = () -> pointService.useUserPoint(userId, amountNegative);

            // then
            assertThatThrownBy(caseZero).isInstanceOf(InvariantViolationException.class);
            assertThatThrownBy(caseNegative).isInstanceOf(InvariantViolationException.class);

            verifyNoInteractions(userPointRepository, pointHistoryRepository);
        }

        // 테스트 작성 이유: 보유하고 있는 포인트를 초과하여 사용하려는 예외를 잘 처리하는지 검증하기 위해
        @Test
        @DisplayName("사용 금액이 보유한 잔액을 초과하면 AmountExceedBalanceException이 발생한다.")
        void givenExceedBalance_whenUsePoint_thenThrowAmountExceedBalanceException() {
            // given
            long userId = 0L;
            long amount = 100L;
            long exceed = 200L;

            when(userPointRepository.findById(userId))
                    .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

            // when
            ThrowableAssert.ThrowingCallable caseExceed = () -> pointService.useUserPoint(userId, exceed);

            // then
            assertThatThrownBy(caseExceed).isInstanceOf(AmountExceedBalanceException.class);

            verify(userPointRepository, never()).save(any(UserPoint.class));
            verify(pointHistoryRepository, never()).save(any(PointHistory.class));
        }

        // 테스트 작성 이유: 트랜잭션의 원자성을 검증하기 위해
        // 추가로 처음에는 롤백을 테스트하려고 했지만, 단위 테스트에서 롤백을 검증하는 것은 제한적이라고 생각하여 예외만 검증
        @Test
        @DisplayName("UserPoint 저장 중 DB 오류가 발생하면 UsePointFailureException이 발생하고, 포인트 내역이 기록되지 않는다.")
        void givenUserPointSaveError_whenUsePoint_thenThrowUsePointFailureException() {
            // given
            when(userPointRepository.findById(anyLong()))
                    .thenReturn(new UserPoint(0L, 100L, System.currentTimeMillis()));
            when(userPointRepository.save(any(UserPoint.class)))
                    .thenThrow(new RuntimeException("DB error"));

            // when
            ThrowableAssert.ThrowingCallable caseUserPoint = () -> pointService.useUserPoint(0L, 100L);

            // then
            assertThatThrownBy(caseUserPoint).isInstanceOf(UsePointFailureException.class);
            verify(pointHistoryRepository, never()).save(any(PointHistory.class));
        }

        // 테스트 작성 이유: 트랜잭션의 원자성을 검증하기 위해
        // 추가로 처음에는 롤백을 테스트하려고 했지만, 단위 테스트에서 롤백을 검증하는 것은 제한적이라고 생각하여 예외만 검증
        @Test
        @DisplayName("PointHistory 저장 중 DB 오류가 발생하면 UsePointFailureException이 발생한다.")
        void givenPointHistorySaveError_whenUsePoint_thenThrowUsePointFailureException() {
            // given
            when(userPointRepository.findById(anyLong()))
                    .thenReturn(new UserPoint(0L, 100L, System.currentTimeMillis()));
            when(userPointRepository.save(any(UserPoint.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pointHistoryRepository.save(any(PointHistory.class)))
                    .thenThrow(new RuntimeException("DB error"));

            // when
            ThrowableAssert.ThrowingCallable casePointHistory = () -> pointService.useUserPoint(0L, 100L);

            // then
            assertThatThrownBy(casePointHistory).isInstanceOf(UsePointFailureException.class);
        }
    }
}
