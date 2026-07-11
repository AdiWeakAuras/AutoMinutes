package com.autominutes.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MeetingDTO(
        Long id,
        String title,
        String description,
        LocalDateTime meetingDate,
        String processingStatus,
        TranscriptDTO transcript,
        List<AttendeeDTO> attendees

) {}