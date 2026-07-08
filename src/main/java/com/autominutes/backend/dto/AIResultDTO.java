package com.autominutes.backend.dto;

import java.util.List;

public record AIResultDTO(
        Long id,
        String summary,
        String detailedSummary,
        String decisions,
        String followUpNotes,
        List<ActionItemDTO> actionItems
) {}