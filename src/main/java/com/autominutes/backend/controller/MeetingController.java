package com.autominutes.backend.controller;

import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.mapper.MeetingMapper;
import com.autominutes.backend.repository.MeetingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingRepository meetingRepository;
    private final MeetingMapper meetingMapper;

    public MeetingController(MeetingRepository meetingRepository, MeetingMapper meetingMapper) {
        this.meetingRepository = meetingRepository;
        this.meetingMapper = meetingMapper;
    }

    @GetMapping
    public List<MeetingDTO> getAllMeetings() {
        return meetingRepository.findAll()
                .stream()
                .map(meetingMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingDTO> getMeetingById(@PathVariable Long id) {
        return meetingRepository.findById(id)
                .map(meetingMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}