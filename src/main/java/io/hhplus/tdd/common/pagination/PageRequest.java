package io.hhplus.tdd.common.pagination;

public record PageRequest(int page, int size) {
    private static final int MAX_SIZE = 100;

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("page 값은 0 이상이어야 합니다.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size 값은 1 이상이어야 합니다.");
        }
        if (size > MAX_SIZE) {
            throw new IllegalArgumentException("size 값은 최대 " + MAX_SIZE + "까지 허용됩니다.");
        }
    }

    public int offset() {
        return page * size;
    }
}
