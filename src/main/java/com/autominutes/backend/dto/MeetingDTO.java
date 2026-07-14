package com.autominutes.backend.dto;

import com.autominutes.backend.entity.ProcessingStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MeetingDTO(
    Long id,
    String title,
    String description,
    LocalDateTime meetingDate,
    ProcessingStatus processingStatus,
    TranscriptDTO transcript,
    List<AttendeeDTO> attendees) {}
