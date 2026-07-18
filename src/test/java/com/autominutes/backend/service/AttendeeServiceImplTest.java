package com.autominutes.backend.service;

import com.autominutes.backend.dto.AttendeeCreateRequest;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequest;
import com.autominutes.backend.entity.Attendee;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.MeetingAttendee;
import com.autominutes.backend.exception.DuplicateResourceException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.mapper.AttendeeMapper;
import com.autominutes.backend.repository.AttendeeRepository;
import com.autominutes.backend.repository.MeetingAttendeeRepository;
import com.autominutes.backend.repository.MeetingRepository;
import com.autominutes.backend.service.impl.AttendeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendeeServiceImplTest {

    @Mock private AttendeeRepository attendeeRepository;
    @Mock private MeetingRepository meetingRepository;
    @Mock private MeetingAttendeeRepository meetingAttendeeRepository;
    @Mock private AttendeeMapper attendeeMapper;

    @InjectMocks
    private AttendeeServiceImpl attendeeService;

    @Test
    void getAttendeesForMeeting_throwsWhenMeetingMissing() {
        when(meetingRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> attendeeService.getAttendeesForMeeting(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAttendeesForMeeting_mapsLinkedAttendees() {
        Attendee attendee = new Attendee();
        MeetingAttendee link = new MeetingAttendee();
        link.setAttendee(attendee);
        AttendeeDTO dto = new AttendeeDTO(1L, "Ana", "ana@example.com", "PM");

        when(meetingRepository.existsById(1L)).thenReturn(true);
        when(meetingAttendeeRepository.findByMeetingId(1L)).thenReturn(List.of(link));
        when(attendeeMapper.toDto(attendee)).thenReturn(dto);

        List<AttendeeDTO> result = attendeeService.getAttendeesForMeeting(1L);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getAttendeeForMeeting_throwsWhenLinkMissing() {
        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendeeService.getAttendeeForMeeting(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAttendeeForMeeting_returnsDtoWhenFound() {
        Attendee attendee = new Attendee();
        MeetingAttendee link = new MeetingAttendee();
        link.setAttendee(attendee);
        AttendeeDTO dto = new AttendeeDTO(2L, "Radu", "radu@example.com", "DEV");

        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 2L))
                .thenReturn(Optional.of(link));
        when(attendeeMapper.toDto(attendee)).thenReturn(dto);

        assertThat(attendeeService.getAttendeeForMeeting(1L, 2L)).isEqualTo(dto);
    }

    @Test
    void addAttendeeToMeeting_throwsWhenMeetingMissing() {
        AttendeeCreateRequest request = new AttendeeCreateRequest("Ana", "ana@example.com", "PM");
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendeeService.addAttendeeToMeeting(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addAttendeeToMeeting_reusesExistingAttendeeByEmail_andLinksIt() {
        AttendeeCreateRequest request = new AttendeeCreateRequest("Ana", "ana@example.com", "PM");
        Meeting meeting = new Meeting();
        Attendee existing = new Attendee();
        existing.setId(5L);
        AttendeeDTO dto = new AttendeeDTO(5L, "Ana", "ana@example.com", "PM");

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(attendeeRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(existing));
        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 5L)).thenReturn(Optional.empty());
        when(attendeeMapper.toDto(existing)).thenReturn(dto);

        AttendeeDTO result = attendeeService.addAttendeeToMeeting(1L, request);

        assertThat(result).isEqualTo(dto);
        verify(attendeeRepository, never()).save(any());
        verify(meetingAttendeeRepository).save(any(MeetingAttendee.class));
    }

    @Test
    void addAttendeeToMeeting_createsNewAttendeeWhenEmailUnknown() {
        AttendeeCreateRequest request = new AttendeeCreateRequest("Maria", "maria@example.com", "STAKEHOLDER");
        Meeting meeting = new Meeting();
        Attendee newEntity = new Attendee();
        Attendee saved = new Attendee();
        saved.setId(9L);
        AttendeeDTO dto = new AttendeeDTO(9L, "Maria", "maria@example.com", "STAKEHOLDER");

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(attendeeRepository.findByEmail("maria@example.com")).thenReturn(Optional.empty());
        when(attendeeMapper.toEntity(request)).thenReturn(newEntity);
        when(attendeeRepository.save(newEntity)).thenReturn(saved);
        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 9L)).thenReturn(Optional.empty());
        when(attendeeMapper.toDto(saved)).thenReturn(dto);

        AttendeeDTO result = attendeeService.addAttendeeToMeeting(1L, request);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void addAttendeeToMeeting_throwsWhenAlreadyLinked() {
        AttendeeCreateRequest request = new AttendeeCreateRequest("Ana", "ana@example.com", "PM");
        Meeting meeting = new Meeting();
        Attendee existing = new Attendee();
        existing.setId(5L);
        MeetingAttendee link = new MeetingAttendee();

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(attendeeRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(existing));
        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 5L)).thenReturn(Optional.of(link));

        assertThatThrownBy(() -> attendeeService.addAttendeeToMeeting(1L, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(meetingAttendeeRepository, never()).save(any());
    }

    @Test
    void updateAttendee_throwsWhenLinkMissing() {
        AttendeeUpdateRequest request = new AttendeeUpdateRequest("New name", null, null);
        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendeeService.updateAttendee(1L, 2L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAttendee_appliesPatchAndSaves() {
        AttendeeUpdateRequest request = new AttendeeUpdateRequest("New name", null, null);
        Attendee attendee = new Attendee();
        MeetingAttendee link = new MeetingAttendee();
        link.setAttendee(attendee);
        AttendeeDTO dto = new AttendeeDTO(2L, "New name", "radu@example.com", "DEV");

        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 2L)).thenReturn(Optional.of(link));
        when(attendeeRepository.save(attendee)).thenReturn(attendee);
        when(attendeeMapper.toDto(attendee)).thenReturn(dto);

        AttendeeDTO result = attendeeService.updateAttendee(1L, 2L, request);

        verify(attendeeMapper).updateEntityFromRequest(request, attendee);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void removeAttendeeFromMeeting_throwsWhenLinkMissing() {
        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendeeService.removeAttendeeFromMeeting(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(meetingAttendeeRepository, never()).delete(any());
    }

    @Test
    void removeAttendeeFromMeeting_deletesLinkWhenFound() {
        MeetingAttendee link = new MeetingAttendee();
        when(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 2L)).thenReturn(Optional.of(link));

        attendeeService.removeAttendeeFromMeeting(1L, 2L);

        verify(meetingAttendeeRepository).delete(link);
    }
}
