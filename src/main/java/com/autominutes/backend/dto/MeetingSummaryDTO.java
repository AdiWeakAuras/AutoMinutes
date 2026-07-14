package com.autominutes.backend.dto;

import com.autominutes.backend.entity.ProcessingStatus;

import java.time.LocalDateTime;

public record MeetingSummaryDTO(
    Long id,
    String title,
    LocalDateTime meetingDate,
    ProcessingStatus processingStatus,
    int attendeeCount) {}
