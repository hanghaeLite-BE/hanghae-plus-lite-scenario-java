package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

  private final UserPointTable userPointTable;
  private final PointHistoryTable pointHistoryTable;

  // 유저별 직렬화를 위한 공정 락 캐시
  private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

  public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
    this.userPointTable = userPointTable;
    this.pointHistoryTable = pointHistoryTable;
  }

  public UserPoint get(long userId) {
    return userPointTable.selectById(userId);
  }

  public List<PointHistory> histories(long userId) {
    return pointHistoryTable.selectAllByUserId(userId);
  }

  public UserPoint charge(long userId, long amount) {
    validateAmount(amount);
    ReentrantLock lock = lockFor(userId);
    lock.lock();
    try {
      UserPoint current = userPointTable.selectById(userId);
      if (willOverflow(current.point(), amount)) {
        throw new IllegalArgumentException("허용 범위를 초과합니다.");
      }
      long newPoint = current.point() + amount;
      UserPoint updated = userPointTable.insertOrUpdate(userId, newPoint);
      pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
      return updated;
    } finally {
      lock.unlock();
    }
  }

  public UserPoint use(long userId, long amount) {
    validateAmount(amount);
    ReentrantLock lock = lockFor(userId);
    lock.lock();
    try {
      UserPoint current = userPointTable.selectById(userId);
      if (current.point() < amount) {
        throw new IllegalStateException("잔액 부족");
      }
      long newPoint = current.point() - amount;
      UserPoint updated = userPointTable.insertOrUpdate(userId, newPoint);
      pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
      return updated;
    } finally {
      lock.unlock();
    }
  }

  // 공정 락: 대기 순서대로 lock 획득 → 기아 방지
  private ReentrantLock lockFor(long userId) {
    return locks.computeIfAbsent(userId, id -> new ReentrantLock(true));
  }

  private void validateAmount(long amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("금액은 0보다 큰 정수여야 합니다.");
    }
  }

  // 오버플로우 & 언더플로우 방지
  private boolean willOverflow(long a, long b) {
    return (b > 0 && a > Long.MAX_VALUE - b) || (b < 0 && a < Long.MIN_VALUE - b);
  }
}