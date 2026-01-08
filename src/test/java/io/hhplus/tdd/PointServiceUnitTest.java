package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PointServiceUnitTest {

  private UserPointTable userPointTable;
  private PointHistoryTable pointHistoryTable;
  private PointService pointService;

  @BeforeEach
  void setUp() {
    userPointTable = mock(UserPointTable.class);
    pointHistoryTable = mock(PointHistoryTable.class);
    pointService = new PointService(userPointTable, pointHistoryTable);
  }

  @Test
  @DisplayName("동일 금액 반복 충전 시")
  void charge_repeatedSameAmount_accumulatesCorrectly() {
    long userId = 1L;
    long[] balance = { 0L }; // <-- Mock 내부 상태

    // selectById는 현재 balance를 반영
    when(userPointTable.selectById(userId))
        .thenAnswer(inv -> new UserPoint(userId, balance[0], System.currentTimeMillis()));

    // insertOrUpdate가 호출될 때 balance를 갱신
    when(userPointTable.insertOrUpdate(eq(userId), anyLong()))
        .thenAnswer(inv -> {
          balance[0] = inv.getArgument(1, Long.class);
          return new UserPoint(userId, balance[0], System.currentTimeMillis());
        });

    int repeats = 5;
    long amount = 100L;
    long expected = 0;

    for (int i = 0; i < repeats; i++) {
      expected += amount;
      UserPoint updated = pointService.charge(userId, amount);
      assertThat(updated.point()).isEqualTo(expected);
    }

    verify(pointHistoryTable, times(repeats))
        .insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    verify(userPointTable, times(repeats)).insertOrUpdate(eq(userId), anyLong());
  }

  @Test
  @DisplayName("음수 금액 사용 시")
  void use_negativeAmount_throwsException() {
    assertThatThrownBy(() -> pointService.use(1L, -100L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("금액은 0보다 큰 정수여야 합니다.");
    verifyNoInteractions(userPointTable, pointHistoryTable);
  }

  @Test
  @DisplayName("잔액 최대치 근처에서 사용 시")
  void use_nearMaxValue_succeeds() {
    long userId = 1L;
    long nearMax = Long.MAX_VALUE - 10;
    when(userPointTable.selectById(userId))
        .thenReturn(new UserPoint(userId, nearMax, System.currentTimeMillis()));
    when(userPointTable.insertOrUpdate(userId, nearMax - 100))
        .thenReturn(new UserPoint(userId, nearMax - 100, System.currentTimeMillis()));

    UserPoint result = pointService.use(userId, 100L);

    assertThat(result.point()).isEqualTo(nearMax - 100);
    verify(pointHistoryTable).insert(eq(userId), eq(100L), eq(TransactionType.USE), anyLong());
  }

  @Test
  @DisplayName("오버플로우가 발생")
  void charge_overflow_throws() {
    long userId = 1L;
    long almostMax = Long.MAX_VALUE - 5;
    when(userPointTable.selectById(userId))
        .thenReturn(new UserPoint(userId, almostMax, System.currentTimeMillis()));

    assertThatThrownBy(() -> pointService.charge(userId, 10L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("허용 범위를 초과합니다.");

    verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
  }

  @Test
  @DisplayName("잔액 부족")
  void use_insufficientBalance_throws() {
    long userId = 1L;
    when(userPointTable.selectById(userId))
        .thenReturn(new UserPoint(userId, 50L, System.currentTimeMillis()));

    assertThatThrownBy(() -> pointService.use(userId, 100L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("잔액 부족");

    verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
  }

  @Test
  @DisplayName("히스토리 빈 리스트 반환")
  void histories_emptyList_returnsEmpty() {
    when(pointHistoryTable.selectAllByUserId(99L)).thenReturn(List.of());

    List<PointHistory> result = pointService.histories(99L);

    assertThat(result).isEmpty();
    verify(pointHistoryTable).selectAllByUserId(99L);
  }

  @Test
  @DisplayName("없는 사용자 포인트 조회")
  void get_nonExistingUser_returnsNull() {
    when(userPointTable.selectById(123L)).thenReturn(null);

    UserPoint result = pointService.get(123L);

    assertThat(result).isNull();
    verify(userPointTable).selectById(123L);
  }
}
