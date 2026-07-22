package com.autominutes.backend.service;

import com.autominutes.backend.dto.TranscriptCreateRequestDTO;
import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.dto.TranscriptUpdateRequestDTO;

/**
 * Service for managing a meeting's transcript.
 * <p>
 * The meeting-transcript relationship is one-to-one: a meeting can have at most one transcript.
 */
public interface TranscriptService {

    /**
     * Returns the transcript associated with a meeting.
     *
     * @param meetingId the id of the meeting
     * @return the transcript DTO
     * @throws com.autominutes.backend.exception.ResourceNotFoundException if the meeting does not exist
     *                                                                     or has no associated transcript
     */
    TranscriptDTO getTranscriptForMeeting(Long meetingId);

    /**
     * Submits the raw transcript for a meeting. Can only be called once per meeting;
     * subsequent corrections should use {@link #updateTranscript}.
     *
     * @param meetingId the id of the meeting
     * @param request   the transcript content (required, may be long text)
     * @return the newly created transcript
     * @throws com.autominutes.backend.exception.ResourceNotFoundException  if the meeting does not exist
     * @throws com.autominutes.backend.exception.DuplicateResourceException if the meeting already has a transcript
     */
    TranscriptDTO submitTranscript(Long meetingId, TranscriptCreateRequestDTO request);

    /**
     * Fully replaces the content of an existing transcript.
     *
     * @param meetingId the id of the meeting
     * @param request   the new content (required)
     * @return the updated transcript
     * @throws com.autominutes.backend.exception.ResourceNotFoundException if the meeting does not exist
     *                                                                     or has no associated transcript
     */
    TranscriptDTO updateTranscript(Long meetingId, TranscriptUpdateRequestDTO request);
}