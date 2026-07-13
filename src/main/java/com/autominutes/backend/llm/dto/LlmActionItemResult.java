package com.autominutes.backend.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw shape of a single action item as returned by the LLM, before validation
 * and mapping into {@code ActionItem} entities.
 * <p>
 * Fields like {@code deadline} and {@code status} are kept as plain strings here
 * on purpose — the model's output is not trusted yet at this stage. Parsing
 * {@code deadline} into a {@link java.time.LocalDate} and validating {@code status}
 * against the allowed values (OPEN, IN_PROGRESS, DONE, UNKNOWN) happens one layer
 * up, in the processing service, where a bad value can be turned into a clear
 * {@code AiProcessingException} instead of a raw parsing crash here.
 */
public record LlmActionItemResult(
        String description,

        @JsonProperty("proposed_assignee")
        String proposedAssignee,

        String deadline,

        String status
) {}