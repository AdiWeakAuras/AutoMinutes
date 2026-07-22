package com.autominutes.backend.dto;

import com.autominutes.backend.entity.ProcessingStatus;

import java.time.LocalDateTime;

public record MeetingUpdateRequestDTO(
    String title,
    String description,
    LocalDateTime meetingDate,
    ProcessingStatus processingStatus) {}
