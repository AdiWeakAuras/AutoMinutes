package com.autominutes.backend.service.impl;

import com.autominutes.backend.dto.TranscriptCreateRequestDTO;
import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.dto.TranscriptUpdateRequestDTO;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.Transcript;
import com.autominutes.backend.exception.DuplicateResourceException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.mapper.TranscriptMapper;
import com.autominutes.backend.repository.MeetingRepository;
import com.autominutes.backend.repository.TranscriptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscriptServiceImplTest {

    @Mock private TranscriptRepository transcriptRepository;
    @Mock private MeetingRepository meetingRepository;
    @Mock private TranscriptMapper transcriptMapper;

    @InjectMocks
    private TranscriptServiceImpl transcriptService;

    @Test
    void getTranscriptForMeeting_throwsWhenMeetingMissing() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transcriptService.getTranscriptForMeeting(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTranscriptForMeeting_throwsWhenNoTranscript() {
        Meeting meeting = new Meeting();
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> transcriptService.getTranscriptForMeeting(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTranscriptForMeeting_returnsDtoWhenPresent() {
        Meeting meeting = new Meeting();
        Transcript transcript = new Transcript();
        meeting.setTranscript(transcript);
        TranscriptDTO dto = new TranscriptDTO(1L, "content", null, List.of());

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(transcriptMapper.toDto(transcript)).thenReturn(dto);

        assertThat(transcriptService.getTranscriptForMeeting(1L)).isEqualTo(dto);
    }

    @Test
    void submitTranscript_throwsWhenMeetingMissing() {
        TranscriptCreateRequestDTO request = new TranscriptCreateRequestDTO("content");
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transcriptService.submitTranscript(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitTranscript_throwsWhenTranscriptAlreadyExists() {
        TranscriptCreateRequestDTO request = new TranscriptCreateRequestDTO("content");
        Meeting meeting = new Meeting();
        meeting.setTranscript(new Transcript());

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> transcriptService.submitTranscript(1L, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(transcriptRepository, never()).save(any());
    }

    @Test
    void submitTranscript_savesAndLinksBothSides() {
        TranscriptCreateRequestDTO request = new TranscriptCreateRequestDTO("Ana: hello");
        Meeting meeting = new Meeting();
        Transcript entity = new Transcript();
        Transcript saved = new Transcript();
        TranscriptDTO dto = new TranscriptDTO(1L, "Ana: hello", null, List.of());

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(transcriptMapper.toEntity(request)).thenReturn(entity);
        when(transcriptRepository.save(entity)).thenReturn(saved);
        when(transcriptMapper.toDto(saved)).thenReturn(dto);

        TranscriptDTO result = transcriptService.submitTranscript(1L, request);

        assertThat(entity.getMeeting()).isEqualTo(meeting);
        assertThat(meeting.getTranscript()).isEqualTo(saved);
        verify(meetingRepository).save(meeting);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void updateTranscript_throwsWhenMeetingMissing() {
        TranscriptUpdateRequestDTO request = new TranscriptUpdateRequestDTO("updated");
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transcriptService.updateTranscript(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTranscript_throwsWhenNoExistingTranscript() {
        TranscriptUpdateRequestDTO request = new TranscriptUpdateRequestDTO("updated");
        Meeting meeting = new Meeting();
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> transcriptService.updateTranscript(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTranscript_updatesContentAndSaves() {
        TranscriptUpdateRequestDTO request = new TranscriptUpdateRequestDTO("updated content");
        Meeting meeting = new Meeting();
        Transcript transcript = new Transcript();
        transcript.setContent("old content");
        meeting.setTranscript(transcript);
        TranscriptDTO dto = new TranscriptDTO(1L, "updated content", null, List.of());

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(transcriptRepository.save(transcript)).thenReturn(transcript);
        when(transcriptMapper.toDto(transcript)).thenReturn(dto);

        TranscriptDTO result = transcriptService.updateTranscript(1L, request);

        assertThat(transcript.getContent()).isEqualTo("updated content");
        assertThat(result).isEqualTo(dto);
    }
}
