package com.autominutes.backend.service.impl;

import com.autominutes.backend.dto.AttendeeCreateRequestDTO;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequestDTO;
import com.autominutes.backend.entity.Attendee;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.MeetingAttendee;
import com.autominutes.backend.exception.DuplicateResourceException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.mapper.AttendeeMapper;
import com.autominutes.backend.repository.AttendeeRepository;
import com.autominutes.backend.repository.MeetingAttendeeRepository;
import com.autominutes.backend.repository.MeetingRepository;
import com.autominutes.backend.service.AttendeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(readOnly = true)
public class AttendeeServiceImpl implements AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingAttendeeRepository meetingAttendeeRepository;
    private final AttendeeMapper attendeeMapper;

    public AttendeeServiceImpl(AttendeeRepository attendeeRepository,
                               MeetingRepository meetingRepository,
                               MeetingAttendeeRepository meetingAttendeeRepository,
                               AttendeeMapper attendeeMapper) {
        this.attendeeRepository = attendeeRepository;
        this.meetingRepository = meetingRepository;
        this.meetingAttendeeRepository = meetingAttendeeRepository;
        this.attendeeMapper = attendeeMapper;
    }

    @Override
    public List<AttendeeDTO> getAttendeesForMeeting(Long meetingId) {
        if (!meetingRepository.existsById(meetingId)) {
            throw ResourceNotFoundException.forMeeting(meetingId);
        }
        return meetingAttendeeRepository.findByMeetingId(meetingId).stream()
                .map(ma -> attendeeMapper.toDto(ma.getAttendee()))
                .toList();
    }

    @Override
    public AttendeeDTO getAttendeeForMeeting(Long meetingId, Long attendeeId) {
        MeetingAttendee link = meetingAttendeeRepository.findByMeetingIdAndAttendeeId(meetingId, attendeeId)
                .orElseThrow(() -> ResourceNotFoundException.forAttendee(attendeeId));
        return attendeeMapper.toDto(link.getAttendee());
    }

    @Override
    @Transactional
    public AttendeeDTO addAttendeeToMeeting(Long meetingId, AttendeeCreateRequestDTO request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

        // cautam dupa email - daca exista deja, il refolosim in loc sa cream unul nou
        Attendee attendee = attendeeRepository.findByEmail(request.email())
                .orElseGet(() -> attendeeRepository.save(attendeeMapper.toEntity(request)));

        // verificam daca e deja legat de ACEST meeting - asta e singurul caz de duplicat real
        boolean alreadyLinked = meetingAttendeeRepository
                .findByMeetingIdAndAttendeeId(meetingId, attendee.getId())
                .isPresent();

        if (alreadyLinked) {
            throw DuplicateResourceException.forAttendeeAlreadyInMeeting(request.email(), meetingId);
        }

        MeetingAttendee link = new MeetingAttendee();
        link.setMeeting(meeting);
        link.setAttendee(attendee);
        meetingAttendeeRepository.save(link);

        return attendeeMapper.toDto(attendee);
    }

    @Override
    @Transactional
    public AttendeeDTO updateAttendee(Long meetingId, Long attendeeId, AttendeeUpdateRequestDTO request) {
        MeetingAttendee link = meetingAttendeeRepository.findByMeetingIdAndAttendeeId(meetingId, attendeeId)
                .orElseThrow(() -> ResourceNotFoundException.forAttendee(attendeeId));

        Attendee attendee = link.getAttendee();
        attendeeMapper.updateEntityFromRequest(request, attendee);
        Attendee saved = attendeeRepository.save(attendee);

        return attendeeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void removeAttendeeFromMeeting(Long meetingId, Long attendeeId) {
        MeetingAttendee link = meetingAttendeeRepository.findByMeetingIdAndAttendeeId(meetingId, attendeeId)
                .orElseThrow(() -> ResourceNotFoundException.forAttendee(attendeeId));

        meetingAttendeeRepository.delete(link);
    }
}