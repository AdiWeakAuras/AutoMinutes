package com.autominutes.backend.dto;

import java.time.LocalDate;

public record ActionItemDTO(
        Long id,
        String description,
        String proposedAssignee,
        LocalDate deadline,
        String status
) {}