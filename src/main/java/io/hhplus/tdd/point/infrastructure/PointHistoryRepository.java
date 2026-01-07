package io.hhplus.tdd.point.infrastructure;

import java.util.List;

import io.hhplus.tdd.common.pagination.PageRequest;
import io.hhplus.tdd.point.domain.PointHistory;

public interface PointHistoryRepository {

	List<PointHistory> findAllByUserId(long userId, PageRequest pageRequest);

    PointHistory save(PointHistory pointHistory);
}
