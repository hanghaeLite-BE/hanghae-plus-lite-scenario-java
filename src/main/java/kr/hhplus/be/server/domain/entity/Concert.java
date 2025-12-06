package kr.hhplus.be.server.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "concerts")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(name = "concert_date", nullable = false)
    private LocalDateTime concertDate;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcertStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Concert() {}

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

    public static Concert create(String title, String artist,
                                 LocalDateTime concertDate, Integer totalSeats,
                                 Integer price) {
        return new Concert(title, artist, concertDate, totalSeats, price);
    }

    public boolean isBookingAvailable() {
        return status == ConcertStatus.AVAILABLE &&
                availableSeats > 0 &&
                concertDate.isAfter(LocalDateTime.now());
    }

    public void updateStatus(ConcertStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
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
