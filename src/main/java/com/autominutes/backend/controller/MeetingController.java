package com.autominutes.backend.controller;

import com.autominutes.backend.dto.MeetingCreateRequest;
import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.dto.MeetingSummaryDTO;
import com.autominutes.backend.dto.MeetingUpdateRequest;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

  private final MeetingService meetingService;

  public MeetingController(MeetingService meetingService) {
    this.meetingService = meetingService;
  }

  @GetMapping
  public List<MeetingSummaryDTO> getAllMeetings() {
    return meetingService.getAllMeetings();
  }

  @GetMapping("/{id}")
  public MeetingDTO getMeetingById(@PathVariable Long id) {
    return meetingService
        .getMeetingById(id)
        .orElseThrow(() -> ResourceNotFoundException.forMeeting(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MeetingDTO createMeeting(@Valid @RequestBody MeetingCreateRequest request) {
    return meetingService.createMeeting(request);
  }

  @PatchMapping("/{id}")
  public MeetingDTO updateMeeting(
      @PathVariable Long id, @Valid @RequestBody MeetingUpdateRequest request) {
    return meetingService.updateMeeting(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMeeting(@PathVariable Long id) {
    meetingService.deleteMeeting(id);
  }
}
