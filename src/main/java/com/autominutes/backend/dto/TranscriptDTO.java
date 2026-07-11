package com.autominutes.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TranscriptDTO(
        Long id,
        String content,
        LocalDateTime createdAt,
        List<AIResultDTO> aiResults
) {}