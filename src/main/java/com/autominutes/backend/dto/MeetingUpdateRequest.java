package com.autominutes.backend.dto;

import com.autominutes.backend.entity.ProcessingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MeetingUpdateRequest(
    String title,
    String description,
    LocalDateTime meetingDate,
    ProcessingStatus processingStatus) {}
