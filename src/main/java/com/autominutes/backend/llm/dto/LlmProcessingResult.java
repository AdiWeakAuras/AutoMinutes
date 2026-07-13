package com.autominutes.backend.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Raw shape of the LLM's structured response for a transcript processing request.
 * <p>
 * This mirrors the JSON structure requested in the prompt (see the processing
 * service), using snake_case field names via {@link JsonProperty} because that
 * is the naming convention we ask the model to follow — models tend to be more
 * reliable with snake_case JSON keys than camelCase.
 * <p>
 * This is intentionally NOT the same as {@code AIResultDTO}: this type represents
 * "whatever the model handed back, parsed as JSON", before it has been validated
 * or turned into persisted entities. {@code AIResultDTO} is the public API shape;
 * this one is a private, internal intermediate step.
 */
public record LlmProcessingResult(
        String summary,

        @JsonProperty("detailed_summary")
        String detailedSummary,

        String decisions,

        @JsonProperty("follow_up_notes")
        String followUpNotes,

        @JsonProperty("action_items")
        List<LlmActionItemResult> actionItems
) {}