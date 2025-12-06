package kr.hhplus.be.server.infrastructure.persistence.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.model.ConcertStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "concerts")
public class ConcertEntity {
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
    private String venue;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcertStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;


    // JPA용 기본 생성자
    protected ConcertEntity() {}
    public enum ConcertStatus {
        SCHEDULED, AVAILABLE, SOLD_OUT, CANCELLED, COMPLETED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDateTime getConcertDate() { return concertDate; }
    public void setConcertDate(LocalDateTime concertDate) { this.concertDate = concertDate; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public ConcertStatus getStatus() { return status; }
    public void setStatus(ConcertStatus status) { this.status = status; }
}