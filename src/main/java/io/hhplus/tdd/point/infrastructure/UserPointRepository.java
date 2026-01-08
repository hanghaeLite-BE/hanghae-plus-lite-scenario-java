package io.hhplus.tdd.point.infrastructure;

import io.hhplus.tdd.point.domain.UserPoint;

public interface UserPointRepository {

    UserPoint findById(long id);

    UserPoint save(UserPoint userPoint);
}
