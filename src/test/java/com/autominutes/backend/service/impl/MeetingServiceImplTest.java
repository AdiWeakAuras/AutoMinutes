package com.autominutes.backend.service.impl;

import com.autominutes.backend.dto.MeetingCreateRequestDTO;
import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.dto.MeetingSummaryDTO;
import com.autominutes.backend.dto.MeetingUpdateRequestDTO;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.ProcessingStatus;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.mapper.MeetingMapper;
import com.autominutes.backend.repository.MeetingRepository;
import com.autominutes.backend.repository.MeetingSummaryProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceImplTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingMapper meetingMapper;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    @Test
    void getAllMeetings_mapsSummaryProjectionsToDtos() {
        MeetingSummaryProjection projection = mock(MeetingSummaryProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getTitle()).thenReturn("Sprint Planning");
        when(projection.getMeetingDate()).thenReturn(LocalDateTime.of(2026, 7, 1, 10, 0));
        when(projection.getProcessingStatus()).thenReturn(ProcessingStatus.PENDING);
        when(projection.getAttendeeCount()).thenReturn(2L);

        when(meetingRepository.findAllSummaries()).thenReturn(List.of(projection));

        List<MeetingSummaryDTO> result = meetingService.getAllMeetings();

        assertThat(result).hasSize(1);
        MeetingSummaryDTO dto = result.get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("Sprint Planning");
        assertThat(dto.attendeeCount()).isEqualTo(2);
        assertThat(dto.processingStatus()).isEqualTo(ProcessingStatus.PENDING);
    }

    @Test
    void getAllMeetings_emptyWhenNoMeetings() {
        when(meetingRepository.findAllSummaries()).thenReturn(List.of());

        assertThat(meetingService.getAllMeetings()).isEmpty();
    }

    @Test
    void getMeetingById_returnsDtoWhenFound() {
        Meeting meeting = new Meeting();
        MeetingDTO dto = new MeetingDTO(1L, "Title", "Desc", LocalDateTime.now(),
                ProcessingStatus.PENDING, null, List.of());

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(meetingMapper.toDto(meeting)).thenReturn(dto);

        Optional<MeetingDTO> result = meetingService.getMeetingById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(dto);
    }

    @Test
    void getMeetingById_returnsEmptyWhenNotFound() {
        when(meetingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(meetingService.getMeetingById(99L)).isEmpty();
    }

    @Test
    void createMeeting_savesAndReturnsDto() {
        MeetingCreateRequestDTO request = new MeetingCreateRequestDTO("Title", "Desc", LocalDateTime.now());
        Meeting entity = new Meeting();
        Meeting saved = new Meeting();
        MeetingDTO dto = new MeetingDTO(1L, "Title", "Desc", LocalDateTime.now(),
                ProcessingStatus.PENDING, null, List.of());

        when(meetingMapper.toEntity(request)).thenReturn(entity);
        when(meetingRepository.save(entity)).thenReturn(saved);
        when(meetingMapper.toDto(saved)).thenReturn(dto);

        MeetingDTO result = meetingService.createMeeting(request);

        assertThat(result).isEqualTo(dto);
        verify(meetingRepository).save(entity);
    }

    @Test
    void updateMeeting_throwsWhenNotFound() {
        MeetingUpdateRequestDTO request = new MeetingUpdateRequestDTO("New title", null, null, null);
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.updateMeeting(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(meetingMapper, never()).updateEntityFromRequest(any(), any());
    }

    @Test
    void updateMeeting_appliesPatchAndSaves() {
        MeetingUpdateRequestDTO request = new MeetingUpdateRequestDTO("New title", null, null, null);
        Meeting meeting = new Meeting();
        MeetingDTO dto = new MeetingDTO(1L, "New title", "Desc", LocalDateTime.now(),
                ProcessingStatus.PENDING, null, List.of());

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(meeting)).thenReturn(meeting);
        when(meetingMapper.toDto(meeting)).thenReturn(dto);

        MeetingDTO result = meetingService.updateMeeting(1L, request);

        verify(meetingMapper).updateEntityFromRequest(request, meeting);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void deleteMeeting_throwsWhenNotFound() {
        when(meetingRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> meetingService.deleteMeeting(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(meetingRepository, never()).deleteById(any());
    }

    @Test
    void deleteMeeting_deletesWhenFound() {
        when(meetingRepository.existsById(1L)).thenReturn(true);

        meetingService.deleteMeeting(1L);

        verify(meetingRepository).deleteById(1L);
    }
}
