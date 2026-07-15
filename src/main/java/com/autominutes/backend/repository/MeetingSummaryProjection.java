package com.autominutes.backend.repository;

import com.autominutes.backend.entity.ProcessingStatus;

import java.time.LocalDateTime;

public interface MeetingSummaryProjection {
  Long getId();

  String getTitle();

  LocalDateTime getMeetingDate();

  ProcessingStatus getProcessingStatus();

  Long getAttendeeCount();
}
