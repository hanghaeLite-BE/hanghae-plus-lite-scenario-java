package kr.hhplus.be.server.domain.concert;

import java.time.LocalDateTime;

public class Concert {
    private Long id;
    private String title;
    private LocalDateTime dateTime;

    public Concert() {}

    public Concert(Long id, String title, LocalDateTime dateTime) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getDateTime() { return dateTime; }
}
