package io.hhplus.tdd.point.application;

import java.util.List;

import io.hhplus.tdd.common.pagination.PageRequest;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;

public interface PointService {

    UserPoint findUserPointById(long id);

	List<PointHistory> findPointHistoriesByUserId(long userId, PageRequest pageRequest);

    UserPoint chargeUserPoint(long userId, long amount);

    UserPoint useUserPoint(long userId, long amount);
}
