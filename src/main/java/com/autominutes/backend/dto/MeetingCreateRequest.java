package com.autominutes.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MeetingCreateRequest(
        @NotBlank(message = "Meeting title cannot be blanked")
        String title,

        String description,

        @NotNull(message = "Meeting date is required")
        LocalDateTime meetingDate
) {}