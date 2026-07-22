package com.autominutes.backend.service.impl;

import com.autominutes.backend.dto.MeetingCreateRequestDTO;
import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.dto.MeetingSummaryDTO;
import com.autominutes.backend.dto.MeetingUpdateRequestDTO;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.mapper.MeetingMapper;
import com.autominutes.backend.repository.MeetingRepository;
import com.autominutes.backend.repository.MeetingSummaryProjection;
import com.autominutes.backend.service.MeetingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MeetingServiceImpl implements MeetingService {

  private final MeetingRepository meetingRepository;
  private final MeetingMapper meetingMapper;

  public MeetingServiceImpl(MeetingRepository meetingRepository, MeetingMapper meetingMapper) {
    this.meetingRepository = meetingRepository;
    this.meetingMapper = meetingMapper;
  }

  @Override
  public List<MeetingSummaryDTO> getAllMeetings() {
    return meetingRepository.findAllSummaries().stream().map(this::toSummaryDto).toList();
  }

  @Override
  public Optional<MeetingDTO> getMeetingById(Long id) {
    return meetingRepository.findById(id).map(meetingMapper::toDto);
  }

  @Override
  @Transactional
  public MeetingDTO createMeeting(MeetingCreateRequestDTO request) {
    Meeting meeting = meetingMapper.toEntity(request);
    Meeting saved = meetingRepository.save(meeting);
    return meetingMapper.toDto(saved);
  }

  @Override
  @Transactional
  public MeetingDTO updateMeeting(Long id, MeetingUpdateRequestDTO request) {
    Meeting meeting =
        meetingRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forMeeting(id));

    meetingMapper.updateEntityFromRequest(request, meeting);
    Meeting saved = meetingRepository.save(meeting);
    return meetingMapper.toDto(saved);
  }

  @Override
  @Transactional
  public void deleteMeeting(Long id) {
    if (!meetingRepository.existsById(id)) {
      throw ResourceNotFoundException.forMeeting(id);
    }
    meetingRepository.deleteById(id);
  }

  private MeetingSummaryDTO toSummaryDto(MeetingSummaryProjection p) {
    return new MeetingSummaryDTO(
        p.getId(),
        p.getTitle(),
        p.getMeetingDate(),
        p.getProcessingStatus(),
        p.getAttendeeCount().intValue());
  }
}
