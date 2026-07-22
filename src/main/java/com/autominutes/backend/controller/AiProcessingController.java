package com.autominutes.backend.controller;

import com.autominutes.backend.dto.AIResultDTO;
import com.autominutes.backend.dto.ProcessMeetingRequestDTO;
import com.autominutes.backend.service.AiProcessingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class AiProcessingController {

    private final AiProcessingService aiProcessingService;

    public AiProcessingController(AiProcessingService aiProcessingService) {
        this.aiProcessingService = aiProcessingService;
    }

    @PostMapping("/{meetingId}/process")
    @ResponseStatus(HttpStatus.CREATED)
    public AIResultDTO processTranscript(@PathVariable Long meetingId,
                                         @RequestBody(required = false) ProcessMeetingRequestDTO request) {
        return aiProcessingService.processTranscript(meetingId, request);
    }

    @GetMapping("/{meetingId}/ai-results")
    public List<AIResultDTO> getAiResults(@PathVariable Long meetingId) {
        return aiProcessingService.getAiResultsForMeeting(meetingId);
    }
}