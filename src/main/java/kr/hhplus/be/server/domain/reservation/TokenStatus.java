package kr.hhplus.be.server.domain.reservation;

public enum TokenStatus {
    ACTIVE("활성"),
    USED("사용됨"),
    EXPIRED("만료됨");

    private final String description;

    TokenStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
