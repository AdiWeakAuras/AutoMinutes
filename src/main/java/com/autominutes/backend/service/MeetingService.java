package com.autominutes.backend.service;

import com.autominutes.backend.dto.MeetingCreateRequest;
import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.dto.MeetingUpdateRequest;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.mapper.MeetingMapper;
import com.autominutes.backend.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingMapper meetingMapper;

    public MeetingService(MeetingRepository meetingRepository, MeetingMapper meetingMapper) {
        this.meetingRepository = meetingRepository;
        this.meetingMapper = meetingMapper;
    }

    public List<MeetingDTO> getAllMeetings() {
        return meetingRepository.findAll()
                .stream()
                .map(meetingMapper::toDto)
                .toList();
    }

    public Optional<MeetingDTO> getMeetingById(Long id) {
        return meetingRepository.findById(id)
                .map(meetingMapper::toDto);
    }

    @Transactional
    public MeetingDTO createMeeting(MeetingCreateRequest request) {
        Meeting meeting = meetingMapper.toEntity(request);
        Meeting saved = meetingRepository.save(meeting);
        return meetingMapper.toDto(saved);
    }

    @Transactional
    public MeetingDTO updateMeeting(Long id, MeetingUpdateRequest request) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forMeeting(id));

        meetingMapper.updateEntityFromRequest(request, meeting);
        Meeting saved = meetingRepository.save(meeting);
        return meetingMapper.toDto(saved);
    }

    @Transactional
    public void deleteMeeting(Long id) {
        if (!meetingRepository.existsById(id)) {
            throw ResourceNotFoundException.forMeeting(id);
        }
        meetingRepository.deleteById(id);
    }
}