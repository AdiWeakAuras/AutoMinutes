package com.autominutes.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "meeting_attendee",
    uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "attendee_id"}))
public class MeetingAttendee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meeting_id", nullable = false)
  private Meeting meeting;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attendee_id", nullable = false)
  private Attendee attendee;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Meeting getMeeting() {
    return meeting;
  }

  public void setMeeting(Meeting meeting) {
    this.meeting = meeting;
  }

  public Attendee getAttendee() {
    return attendee;
  }

  public void setAttendee(Attendee attendee) {
    this.attendee = attendee;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
