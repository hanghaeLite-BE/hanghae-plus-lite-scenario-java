package io.hhplus.tdd.point.infrastructure;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointRepositoryImpl implements UserPointRepository {

	private final UserPointTable userPointTable;

	@Override
	public UserPoint findById(long id) {
		return userPointTable.selectById(id);
	}

    @Override
    public UserPoint save(UserPoint userPoint) {
        long id = userPoint.id();
        long amount = userPoint.point();

        return userPointTable.insertOrUpdate(id, amount);
    }
}
