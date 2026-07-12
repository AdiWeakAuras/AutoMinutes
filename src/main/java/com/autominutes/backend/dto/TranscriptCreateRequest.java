package com.autominutes.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TranscriptCreateRequest(
        @NotBlank(message = "The content field cannot be blank")
        String content
) {}