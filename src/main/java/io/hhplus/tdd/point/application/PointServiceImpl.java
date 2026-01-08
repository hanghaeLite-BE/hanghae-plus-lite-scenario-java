package io.hhplus.tdd.point.application;

import static io.hhplus.tdd.point.domain.TransactionType.*;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import io.hhplus.tdd.common.pagination.PageRequest;
import org.springframework.stereotype.Service;

import io.hhplus.tdd.common.LockManager;
import io.hhplus.tdd.common.exception.AmountExceedBalanceException;
import io.hhplus.tdd.common.exception.ChargePointFailureException;
import io.hhplus.tdd.common.exception.InvariantViolationException;
import io.hhplus.tdd.common.exception.UsePointFailureException;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.infrastructure.PointHistoryRepository;
import io.hhplus.tdd.point.infrastructure.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

	private final LockManager lockManager;

	private final UserPointRepository userPointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	private static final long MAX_AMOUNT = 100_000_000L; // 임의의 포인트 최대값 설정

	private static final String CHARGE_POINT_FAIL = "포인트 충전에 실패했습니다. 잠시 후 다시 시도해주세요.";
	private static final String USE_POINT_FAIL    = "포인트 사용에 실패했습니다. 잠시 후 다시 시도해주세요.";

	@Override
	public UserPoint findUserPointById(long id) {
		return userPointRepository.findById(id);
	}

	@Override
	public List<PointHistory> findPointHistoriesByUserId(long userId, PageRequest pageRequest) {
		return pointHistoryRepository.findAllByUserId(userId, pageRequest);
	}

	@Override
	public UserPoint chargeUserPoint(long userId, long amount) {
		validAmount(amount);

		ReentrantLock lock = lockManager.acquire(userId);
		lock.lock();

		try {
			UserPoint userPoint = userPointRepository.findById(userId);

			final long current = userPoint.point();
			if (current >= MAX_AMOUNT || amount > (MAX_AMOUNT - current)) {
				throw new AmountExceedBalanceException("최대 포인트 한도를 초과했습니다. (현재: " + current + ", 한도: " + MAX_AMOUNT + ')');
			}

			UserPoint savedUserPoint = saveUserPoint(userPoint, userId, amount, CHARGE);
			savePointHistory(amount, savedUserPoint, userPoint, CHARGE);

			return savedUserPoint;
		} finally {
			lock.unlock();
            lockManager.release(userId);
		}
	}

	@Override
	public UserPoint useUserPoint(long userId, long amount) {
		validAmount(amount);

		ReentrantLock lock = lockManager.acquire(userId);
		lock.lock();

		try {
			UserPoint userPoint = userPointRepository.findById(userId);
			if (userPoint.point() < amount) {
				throw new AmountExceedBalanceException("포인트 잔액이 부족합니다. (현재 잔액: " + userPoint.point() + ')');
			}

			UserPoint savedUserPoint = saveUserPoint(userPoint, userId, amount, USE);
			savePointHistory(amount, savedUserPoint, userPoint, USE);

			return savedUserPoint;
		} finally {
			lock.unlock();
            lockManager.release(userId);
		}
	}

	private void validAmount(long amount) {
		if (amount <= 0) {
			log.warn("amount 값은 0보다 커야 합니다. (입력값: {})", amount);
			throw new InvariantViolationException();
		}
	}

	private UserPoint saveUserPoint(UserPoint userPoint, long userId, long amount, TransactionType type) {
		long prevPoint = userPoint.point();
		long newPoint = type == CHARGE ? Math.addExact(prevPoint, amount) : Math.subtractExact(prevPoint, amount);
		UserPoint newUserPoint = new UserPoint(userId, newPoint, System.currentTimeMillis());

		UserPoint savedUserPoint;
		try {
			savedUserPoint = userPointRepository.save(newUserPoint);
		} catch (Exception e) {
			log.error("UserPoint 저장에 실패했습니다. ({})", newUserPoint);

			if (type == CHARGE) {
				throw new ChargePointFailureException(CHARGE_POINT_FAIL, e);
			} else {
				throw new UsePointFailureException(USE_POINT_FAIL, e);
			}
		}

		return savedUserPoint;
	}

	private void savePointHistory(long amount, UserPoint newUserPoint, UserPoint oldUserPoint, TransactionType type) {
		long tempId = 0L; // id는 auto-increment
		long savedUserId = newUserPoint.id();
		PointHistory pointHistory = new PointHistory(tempId, savedUserId, amount, type, System.currentTimeMillis());

		try {
			pointHistoryRepository.save(pointHistory);
		} catch (Exception e) {
			log.error("PointHistory 저장에 실패했습니다. ({})", pointHistory);
			rollbackUserPoint(oldUserPoint, newUserPoint);

			if (type == CHARGE) {
				throw new ChargePointFailureException(CHARGE_POINT_FAIL, e);
			} else {
				throw new UsePointFailureException(USE_POINT_FAIL, e);
			}
		}
	}

	private void rollbackUserPoint(UserPoint prev, UserPoint curr) {
		log.info("UserPoint 롤백을 시작합니다. ({})", curr);

		UserPoint result;
		try {
			result = userPointRepository.save(prev);
		} catch (Exception e) {
			throw new RuntimeException("UserPoint 롤백에 실패했습니다.", e);
		}

		log.info("UserPoint 롤백이 완료됐습니다. ({})", result);
	}
}
