package com.autominutes.backend.controller;

import com.autominutes.backend.dto.AttendeeCreateRequestDTO;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequestDTO;
import com.autominutes.backend.service.AttendeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings/{meetingId}/attendees")
public class AttendeeController {

    private final AttendeeService attendeeService;

    public AttendeeController(AttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    @GetMapping
    public List<AttendeeDTO> getAttendeesForMeeting(@PathVariable Long meetingId) {
        return attendeeService.getAttendeesForMeeting(meetingId);
    }

    @GetMapping("/{attendeeId}")
    public AttendeeDTO getAttendeeById(@PathVariable Long meetingId, @PathVariable Long attendeeId) {
        return attendeeService.getAttendeeForMeeting(meetingId, attendeeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendeeDTO addAttendee(@PathVariable Long meetingId,
                                   @Valid @RequestBody AttendeeCreateRequestDTO request) {
        return attendeeService.addAttendeeToMeeting(meetingId, request);
    }

    @PatchMapping("/{attendeeId}")
    public AttendeeDTO updateAttendee(@PathVariable Long meetingId,
                                      @PathVariable Long attendeeId,
                                      @Valid @RequestBody AttendeeUpdateRequestDTO request) {
        return attendeeService.updateAttendee(meetingId, attendeeId, request);
    }

    @DeleteMapping("/{attendeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAttendee(@PathVariable Long meetingId, @PathVariable Long attendeeId) {
        attendeeService.removeAttendeeFromMeeting(meetingId, attendeeId);
    }
}