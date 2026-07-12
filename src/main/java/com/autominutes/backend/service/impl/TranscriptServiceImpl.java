package com.autominutes.backend.service.impl;

import com.autominutes.backend.dto.TranscriptCreateRequest;
import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.dto.TranscriptUpdateRequest;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.Transcript;
import com.autominutes.backend.exception.DuplicateResourceException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.mapper.TranscriptMapper;
import com.autominutes.backend.repository.MeetingRepository;
import com.autominutes.backend.repository.TranscriptRepository;
import com.autominutes.backend.service.TranscriptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementare standard a {@link TranscriptService}, bazata pe Spring Data JPA si MapStruct.
 */
@Service
@Transactional(readOnly = true)
public class TranscriptServiceImpl implements TranscriptService {

    private final TranscriptRepository transcriptRepository;
    private final MeetingRepository meetingRepository;
    private final TranscriptMapper transcriptMapper;

    public TranscriptServiceImpl(TranscriptRepository transcriptRepository,
                                 MeetingRepository meetingRepository,
                                 TranscriptMapper transcriptMapper) {
        this.transcriptRepository = transcriptRepository;
        this.meetingRepository = meetingRepository;
        this.transcriptMapper = transcriptMapper;
    }

    @Override
    public TranscriptDTO getTranscriptForMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

        Transcript transcript = meeting.getTranscript();
        if (transcript == null) {
            throw ResourceNotFoundException.forTranscript(meetingId);
        }
        return transcriptMapper.toDto(transcript);
    }

    @Override
    @Transactional
    public TranscriptDTO submitTranscript(Long meetingId, TranscriptCreateRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

        if (meeting.getTranscript() != null) {
            throw DuplicateResourceException.forTranscript(meetingId);
        }

        Transcript transcript = transcriptMapper.toEntity(request);
        transcript.setMeeting(meeting);
        Transcript savedTranscript = transcriptRepository.save(transcript);

        // completam legatura si din partea meeting-ului (relatie 1-la-1 bidirectionala)
        meeting.setTranscript(savedTranscript);
        meetingRepository.save(meeting);

        return transcriptMapper.toDto(savedTranscript);
    }

    @Override
    @Transactional
    public TranscriptDTO updateTranscript(Long meetingId, TranscriptUpdateRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

        Transcript transcript = meeting.getTranscript();
        if (transcript == null) {
            throw ResourceNotFoundException.forTranscript(meetingId);
        }

        transcript.setContent(request.content());
        Transcript saved = transcriptRepository.save(transcript);

        return transcriptMapper.toDto(saved);
    }
}