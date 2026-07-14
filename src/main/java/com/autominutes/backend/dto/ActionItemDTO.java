package com.autominutes.backend.dto;

import com.autominutes.backend.entity.ActionItemStatus;

import java.time.LocalDate;

public record ActionItemDTO(
    Long id,
    String description,
    String proposedAssignee,
    LocalDate deadline,
    ActionItemStatus status) {}
