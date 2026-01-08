package io.hhplus.tdd.common.pagination;

import java.util.List;
import java.util.Objects;

public final class PaginationManager {

    private PaginationManager() {}

    public static <T> List<T> paging(List<T> source, PageRequest request) {
        Objects.requireNonNull(source, "source 값은 null일 수 없습니다.");
        Objects.requireNonNull(request, "request 값은 null일 수 없습니다.");

        int offset = request.offset();
        if (offset >= source.size()) {
            return List.of();
        }

        int limit = Math.min(offset + request.size(), source.size());

        return source.subList(offset, limit);
    }
}
