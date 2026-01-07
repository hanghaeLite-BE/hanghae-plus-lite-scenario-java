package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class PointConcurrencyTest {

  private static final long USER_ID = 1L;

  private void runConcurrent(int n, Runnable task) throws InterruptedException {
    ExecutorService pool = Executors.newFixedThreadPool(Math.min(n, 32));
    CountDownLatch ready = new CountDownLatch(n);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(n);

    for (int i = 0; i < n; i++) {
      pool.submit(() -> {
        ready.countDown(); // 준비 완료
        try {
          start.await(); // 동시에 출발
          task.run();
        } catch (InterruptedException ignored) {
        } finally {
          done.countDown();
        }
      });
    }
    ready.await();
    start.countDown();
    done.await();
    pool.shutdown();
  }

  @Test
  @DisplayName("동시에 여러 충전 요청이 들어와도 정확하게 누적된다")
  void 동시에_여러_충전_요청_정확히_누적() throws InterruptedException {
    UserPointTable userPointTable = new UserPointTable();
    PointHistoryTable historyTable = new PointHistoryTable();
    PointService service = new PointService(userPointTable, historyTable);

    userPointTable.insertOrUpdate(USER_ID, 0L);

    int threads = 10;
    long amount = 100;

    // 동시 충전
    runConcurrent(threads, () -> service.charge(USER_ID, amount));

    UserPoint result = service.get(USER_ID);
    assertThat(result.point()).isEqualTo(threads * amount);
    assertThat(historyTable.selectAllByUserId(USER_ID)).hasSize(threads); // 히스토리도 일치
  }

  @Test
  @DisplayName("동시에 충전과 사용 요청이 섞여도 정확히 처리된다 (2-페이즈로 결정적)")
  void 충전_후_사용_결정적_시나리오() throws InterruptedException {
    UserPointTable userPointTable = new UserPointTable();
    PointHistoryTable historyTable = new PointHistoryTable();
    PointService service = new PointService(userPointTable, historyTable);

    userPointTable.insertOrUpdate(USER_ID, 1000L);

    int threads = 10;
    long chargeAmount = 100;
    long useAmount = 50;

    // 충전만 동시
    runConcurrent(threads, () -> service.charge(USER_ID, chargeAmount));

    // 사용만 동시 (충분한 잔액 확보 상태 → 실패 없음)
    runConcurrent(threads, () -> service.use(USER_ID, useAmount));

    long expected = 1000L + threads * chargeAmount - threads * useAmount;
    assertThat(service.get(USER_ID).point()).isEqualTo(expected);
    assertThat(historyTable.selectAllByUserId(USER_ID)).hasSize(threads * 2);
  }

  @Test
  @DisplayName("서로 다른 유저의 요청은 병렬로 처리된다(유저별 락 분리)")
  void 다른_유저_병렬_처리() throws InterruptedException {
    UserPointTable userPointTable = new UserPointTable();
    PointHistoryTable historyTable = new PointHistoryTable();
    PointService service = new PointService(userPointTable, historyTable);

    long U1 = 1L, U2 = 2L;
    userPointTable.insertOrUpdate(U1, 0L);
    userPointTable.insertOrUpdate(U2, 0L);

    int threads = 10;
    long amount = 100;

    // U1/U2 작업을 섞어서 동시에 던짐
    ExecutorService pool = Executors.newFixedThreadPool(32);
    CountDownLatch ready = new CountDownLatch(threads * 2);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads * 2);
    List<Callable<Void>> tasks = new ArrayList<>();

    for (int i = 0; i < threads; i++) {
      tasks.add(() -> {
        ready.countDown();
        start.await();
        service.charge(U1, amount);
        done.countDown();
        return null;
      });
      tasks.add(() -> {
        ready.countDown();
        start.await();
        service.charge(U2, amount);
        done.countDown();
        return null;
      });
    }
    tasks.forEach(pool::submit);
    ready.await();
    start.countDown();
    done.await();
    pool.shutdown();

    assertThat(service.get(U1).point()).isEqualTo(threads * amount);
    assertThat(service.get(U2).point()).isEqualTo(threads * amount);
  }
}