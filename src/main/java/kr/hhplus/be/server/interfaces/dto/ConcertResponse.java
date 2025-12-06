package kr.hhplus.be.server.interfaces.dto;

import kr.hhplus.be.server.domain.model.Concert;
import kr.hhplus.be.server.domain.model.ConcertStatus;

import java.time.LocalDateTime;

public class ConcertResponse {

    private final Long id;
    private final String title;
    private final String artist;
    private final LocalDateTime concertDate;
    private final Integer totalSeats;
    private final Integer availableSeats;
    private final Integer price;
    private final ConcertStatus status;
    private final boolean isBookingAvailable;

    private ConcertResponse(Long id, String title, String artist,
                            LocalDateTime concertDate, Integer totalSeats,
                            Integer availableSeats, Integer price,
                            ConcertStatus status, boolean isBookingAvailable) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.concertDate = concertDate;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.price = price;
        this.status = status;
        this.isBookingAvailable = isBookingAvailable;
    }

    public static ConcertResponse from(Concert concert) {
        return new ConcertResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getArtist(),
                concert.getConcertDate(),
                concert.getTotalSeats(),
                concert.getAvailableSeats(),
                concert.getPrice(),
                concert.getStatus(),
                concert.isBookingAvailable()
        );
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
    public boolean getIsBookingAvailable() { return isBookingAvailable; }
}
