package com.autominutes.backend.service;

import com.autominutes.backend.dto.AttendeeCreateRequest;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequest;
import com.autominutes.backend.entity.Attendee;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.MeetingAttendee;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.mapper.AttendeeMapper;
import com.autominutes.backend.repository.AttendeeRepository;
import com.autominutes.backend.repository.MeetingAttendeeRepository;
import com.autominutes.backend.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingAttendeeRepository meetingAttendeeRepository;
    private final AttendeeMapper attendeeMapper;

    public AttendeeService(AttendeeRepository attendeeRepository,
                           MeetingRepository meetingRepository,
                           MeetingAttendeeRepository meetingAttendeeRepository,
                           AttendeeMapper attendeeMapper) {
        this.attendeeRepository = attendeeRepository;
        this.meetingRepository = meetingRepository;
        this.meetingAttendeeRepository = meetingAttendeeRepository;
        this.attendeeMapper = attendeeMapper;
    }

    public List<AttendeeDTO> getAttendeesForMeeting(Long meetingId) {
        if (!meetingRepository.existsById(meetingId)) {
            throw ResourceNotFoundException.forMeeting(meetingId);
        }
        return meetingAttendeeRepository.findByMeetingId(meetingId).stream()
                .map(ma -> attendeeMapper.toDto(ma.getAttendee()))
                .toList();
    }

    @Transactional
    public AttendeeDTO addAttendeeToMeeting(Long meetingId, AttendeeCreateRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

        Attendee attendee = findOrCreateAttendee(request);

        // searches for duplicates
        boolean alreadyLinked = meetingAttendeeRepository
                .findByMeetingIdAndAttendeeId(meetingId, attendee.getId())
                .isPresent();

        if (!alreadyLinked) {
            MeetingAttendee link = new MeetingAttendee();
            link.setMeeting(meeting);
            link.setAttendee(attendee);
            meetingAttendeeRepository.save(link);
        }

        return attendeeMapper.toDto(attendee);
    }

    private Attendee findOrCreateAttendee(AttendeeCreateRequest request) {
        // searches after email
        if (request.email() != null && !request.email().isBlank()) {
            return attendeeRepository.findByEmail(request.email())
                    .orElseGet(() -> attendeeRepository.save(attendeeMapper.toEntity(request)));
        }
        // without email, it creates a new attendee every time
        return attendeeRepository.save(attendeeMapper.toEntity(request));
    }

    @Transactional
    public AttendeeDTO updateAttendee(Long meetingId, Long attendeeId, AttendeeUpdateRequest request) {
        MeetingAttendee link = meetingAttendeeRepository.findByMeetingIdAndAttendeeId(meetingId, attendeeId)
                .orElseThrow(() -> ResourceNotFoundException.forAttendee(attendeeId));

        Attendee attendee = link.getAttendee();
        attendeeMapper.updateEntityFromRequest(request, attendee);
        Attendee saved = attendeeRepository.save(attendee);

        return attendeeMapper.toDto(saved);
    }

    @Transactional
    public void removeAttendeeFromMeeting(Long meetingId, Long attendeeId) {
        MeetingAttendee link = meetingAttendeeRepository.findByMeetingIdAndAttendeeId(meetingId, attendeeId)
                .orElseThrow(() -> ResourceNotFoundException.forAttendee(attendeeId));

        meetingAttendeeRepository.delete(link);
    }
}