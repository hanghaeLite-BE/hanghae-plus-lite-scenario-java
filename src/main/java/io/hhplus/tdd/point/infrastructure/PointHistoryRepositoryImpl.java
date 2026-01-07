package io.hhplus.tdd.point.infrastructure;

import java.util.List;

import io.hhplus.tdd.common.pagination.PageRequest;
import io.hhplus.tdd.common.pagination.PaginationManager;
import io.hhplus.tdd.point.domain.TransactionType;
import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.domain.PointHistory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

	private final PointHistoryTable pointHistoryTable;

	@Override
	public List<PointHistory> findAllByUserId(long userId, PageRequest pageRequest) {
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);

        return PaginationManager.paging(pointHistories, pageRequest);
	}

    @Override
    public PointHistory save(PointHistory pointHistory) {
        long userId = pointHistory.userId();
        long amount = pointHistory.amount();
        TransactionType type = pointHistory.type();
        long updateMillis = pointHistory.updateMillis();

        return pointHistoryTable.insert(userId, amount, type, updateMillis);
    }
}
