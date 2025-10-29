package com.example.meeting.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Column(length = 50)
    private String status; // Scheduled, Completed, Cancelled
    
    @Column(length = 500)
    private String location;
    
    @Column(columnDefinition = "TEXT")
    private String participants;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    // --- Constructors ---
    public Meeting() {}

    public Meeting(String title, String description, LocalDateTime startTime, LocalDateTime endTime, String status, User createdBy) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdBy = createdBy;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meeting meeting = (Meeting) o;
        return id != null && id.equals(meeting.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
