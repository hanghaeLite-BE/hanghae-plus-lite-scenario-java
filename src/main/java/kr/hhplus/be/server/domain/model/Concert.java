package kr.hhplus.be.server.domain.model;

import java.time.LocalDateTime;

public class Concert {
    private Long id;
    private final String title;
    private final String artist;
    private final LocalDateTime concertDate;
    private final Integer totalSeats;
    private Integer availableSeats;
    private final Integer price;
    private ConcertStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 생성자
    private Concert(String title, String artist, LocalDateTime concertDate,
                    Integer totalSeats, Integer price) {
        this.title = title;
        this.artist = artist;
        this.concertDate = concertDate;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.price = price;
        this.status = ConcertStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 팩토리 메서드
    public static Concert create(String title, String artist,
                                 LocalDateTime concertDate, Integer totalSeats,
                                 Integer price) {
        validateInput(title, artist, concertDate, totalSeats, price);
        return new Concert(title, artist, concertDate, totalSeats, price);
    }

    // 비즈니스 규칙 메서드
    public boolean isBookingAvailable() {
        return status == ConcertStatus.AVAILABLE &&
                availableSeats > 0 &&
                concertDate.isAfter(LocalDateTime.now());
    }

    public void updateStatus(ConcertStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("상태는 null일 수 없습니다.");
        }
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void decreaseAvailableSeats() {
        if (availableSeats <= 0) {
            throw new IllegalStateException("더 이상 예약 가능한 좌석이 없습니다.");
        }
        this.availableSeats--;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseAvailableSeats() {
        if (availableSeats >= totalSeats) {
            throw new IllegalStateException("가용 좌석이 총 좌석을 초과할 수 없습니다.");
        }
        this.availableSeats++;
        this.updatedAt = LocalDateTime.now();
    }

    // 입력값 검증
    private static void validateInput(String title, String artist, LocalDateTime concertDate,
                                      Integer totalSeats, Integer price) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("콘서트 제목은 필수입니다.");
        }
        if (artist == null || artist.trim().isEmpty()) {
            throw new IllegalArgumentException("아티스트명은 필수입니다.");
        }
        if (concertDate == null) {
            throw new IllegalArgumentException("콘서트 날짜는 필수입니다.");
        }
        if (concertDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("콘서트 날짜는 현재 시간 이후여야 합니다.");
        }
        if (totalSeats == null || totalSeats <= 0) {
            throw new IllegalArgumentException("총 좌석 수는 1 이상이어야 합니다.");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
    }

    // Infrastructure에서만 사용하는 메서드 (package-private)
    void assignId(Long id) {
        this.id = id;
    }

    void updateAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    void updateUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public LocalDateTime getConcertDate() { return concertDate; }
    public Integer getTotalSeats() { return totalSeats; }
    public Integer getAvailableSeats() { return availableSeats; }
    public Integer getPrice() { return price; }
    public ConcertStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}