package com.autominutes.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TranscriptUpdateRequestDTO(
        @NotBlank(message = "The content field cannot be blank")
        String content
) {}