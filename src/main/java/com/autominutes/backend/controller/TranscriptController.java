package com.autominutes.backend.controller;

import com.autominutes.backend.dto.TranscriptCreateRequest;
import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.dto.TranscriptUpdateRequest;
import com.autominutes.backend.service.TranscriptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings/{id}/transcript")
public class TranscriptController {

    private final TranscriptService transcriptService;

    public TranscriptController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    @GetMapping
    public TranscriptDTO getTranscript(@PathVariable Long id) {
        return transcriptService.getTranscriptForMeeting(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TranscriptDTO submitTranscript(@PathVariable Long id,
                                          @Valid @RequestBody TranscriptCreateRequest request) {
        return transcriptService.submitTranscript(id, request);
    }

    @PutMapping
    public TranscriptDTO updateTranscript(@PathVariable Long id,
                                          @Valid @RequestBody TranscriptUpdateRequest request) {
        return transcriptService.updateTranscript(id, request);
    }
}