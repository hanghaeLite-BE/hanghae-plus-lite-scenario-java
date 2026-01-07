package io.hhplus.tdd.point.infrastructure;

import io.hhplus.tdd.common.pagination.PageRequest;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointHistoryRepositoryTest {

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointHistoryRepositoryImpl pointHistoryRepository;

    // 테스트 작성 이유: 페이지네이션 로직이 정상적으로 동작하는지 검증하기 위해
    @Test
    @DisplayName("PageRequest가 주어지면 페이지네이션된 PointHistory 리스트가 반환된다.")
    void givenPageRequest_whenFindAllByUserId_thenReturnPaginatedPointHistoryList() {
        // given
        long userId = 0L;
        List<PointHistory> pointHistories = IntStream.range(0, 15)
                .mapToObj(i -> new PointHistory(i, userId, 100L, TransactionType.CHARGE, System.currentTimeMillis()))
                .toList();
        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(pointHistories);

        PageRequest page1 = new PageRequest(0, 10);
        PageRequest page2 = new PageRequest(1, 10);
        PageRequest page3 = new PageRequest(2, 10);

        // when
        List<PointHistory> size10List = pointHistoryRepository.findAllByUserId(userId, page1);
        List<PointHistory> size5List = pointHistoryRepository.findAllByUserId(userId, page2);
        List<PointHistory> size0List = pointHistoryRepository.findAllByUserId(userId, page3);

        // then
        assertThat(size10List).hasSize(10);
        assertThat(size5List).hasSize(5);
        assertThat(size0List).isEmpty();
    }
}