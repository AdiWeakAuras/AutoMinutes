package com.autominutes.backend.dto;

import java.time.LocalDateTime;

public record TranscriptDTO(
        Long id,
        String content,
        LocalDateTime createdAt
) {}