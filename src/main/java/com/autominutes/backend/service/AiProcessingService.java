package com.autominutes.backend.service;

import com.autominutes.backend.dto.AIResultDTO;
import com.autominutes.backend.dto.ProcessMeetingRequest;

import java.util.List;

/**
 * Service that orchestrates AI processing of a meeting's transcript.
 * <p>
 * (Regeneration / Reprocessing) of the backend specification. Reprocessing is
 * supported implicitly: calling {@link #processTranscript} again for the same
 * meeting creates a new {@code AIResult} without deleting previous ones, since
 * a transcript can have multiple AI results over time.
 */
public interface AiProcessingService {

    /**
     * Processes a meeting's transcript through the configured LLM, producing a
     * structured summary, decisions, follow-up notes, and action items, and
     * persists the result.
     * <p>
     * Follows the workflow from requirement 8: validates the meeting has a
     * non-empty transcript, resolves the prompt template to use (explicit or
     * default), builds the final prompt, calls the LLM, validates and maps its
     * response, and persists the {@code AIResult} together with its
     * {@code ActionItem}s. On success, the meeting's processing status is
     * updated to {@code COMPLETED}.
     *
     * @param meetingId the id of the meeting to process
     * @param request   optional request body; {@code promptTemplateId} may specify
     *                  which prompt template to use instead of the default one
     * @return the newly created AI result, including its action items
     * @throws com.autominutes.backend.exception.ResourceNotFoundException if the meeting does not exist,
     *                                                                     has no transcript, or the
     *                                                                     requested prompt template does not exist
     * @throws com.autominutes.backend.exception.InvalidRequestException  if the meeting's transcript is empty
     * @throws com.autominutes.backend.llm.LlmCommunicationException      if the LLM provider cannot be reached
     * @throws com.autominutes.backend.exception.AiProcessingException    if the LLM's response cannot be parsed
     *                                                                    or does not match the expected schema
     */
    AIResultDTO processTranscript(Long meetingId, ProcessMeetingRequest request);

    /**
     * Lists all AI results generated for a meeting's transcript, including
     * older ones from previous processing runs (see requirement 3.7 — previous
     * results remain traceable).
     *
     * @param meetingId the id of the meeting
     * @return list of AI results; empty if the meeting has no transcript or no AI results yet
     * @throws com.autominutes.backend.exception.ResourceNotFoundException if the meeting does not exist
     */
    List<AIResultDTO> getAiResultsForMeeting(Long meetingId);
}