package com.autominutes.backend.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@ConfigurationProperties(prefix = "llm")
@Validated
public record LlmProperties(
        @NotBlank(message = "llm.base-url must be configured")
        String baseUrl,

        @NotBlank(message = "llm.model must be configured")
        String model,

        @Positive(message = "llm.timeout-seconds must be a positive number")
        int timeoutSeconds
) {}