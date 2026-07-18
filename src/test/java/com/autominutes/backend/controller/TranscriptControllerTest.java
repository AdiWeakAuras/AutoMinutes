package com.autominutes.backend.controller;

import com.autominutes.backend.dto.TranscriptCreateRequest;
import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.dto.TranscriptUpdateRequest;
import com.autominutes.backend.exception.DuplicateResourceException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.service.TranscriptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranscriptController.class)
class TranscriptControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private TranscriptService transcriptService;

    @Test
    void getTranscript_returns200WhenPresent() throws Exception {
        TranscriptDTO dto = new TranscriptDTO(1L, "content here", null, List.of());
        when(transcriptService.getTranscriptForMeeting(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/meetings/1/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("content here"));
    }

    @Test
    void getTranscript_returns404WhenMissing() throws Exception {
        when(transcriptService.getTranscriptForMeeting(1L))
                .thenThrow(ResourceNotFoundException.forTranscript(1L));

        mockMvc.perform(get("/api/meetings/1/transcript"))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitTranscript_returns201WhenValid() throws Exception {
        TranscriptCreateRequest request = new TranscriptCreateRequest("Ana: hello");
        TranscriptDTO dto = new TranscriptDTO(1L, "Ana: hello", null, List.of());
        when(transcriptService.submitTranscript(eq(1L), any(TranscriptCreateRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/meetings/1/transcript")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Ana: hello"));
    }

    @Test
    void submitTranscript_returns400WhenContentBlank() throws Exception {
        TranscriptCreateRequest request = new TranscriptCreateRequest("");

        mockMvc.perform(post("/api/meetings/1/transcript")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitTranscript_returns409WhenAlreadyExists() throws Exception {
        TranscriptCreateRequest request = new TranscriptCreateRequest("Ana: hello");
        when(transcriptService.submitTranscript(eq(1L), any(TranscriptCreateRequest.class)))
                .thenThrow(DuplicateResourceException.forTranscript(1L));

        mockMvc.perform(post("/api/meetings/1/transcript")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateTranscript_returns200() throws Exception {
        TranscriptUpdateRequest request = new TranscriptUpdateRequest("updated content");
        TranscriptDTO dto = new TranscriptDTO(1L, "updated content", null, List.of());
        when(transcriptService.updateTranscript(eq(1L), any(TranscriptUpdateRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/meetings/1/transcript")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("updated content"));
    }

    @Test
    void updateTranscript_returns400WhenContentBlank() throws Exception {
        TranscriptUpdateRequest request = new TranscriptUpdateRequest(" ");

        mockMvc.perform(put("/api/meetings/1/transcript")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTranscript_returns404WhenNoTranscriptYet() throws Exception {
        TranscriptUpdateRequest request = new TranscriptUpdateRequest("updated content");
        when(transcriptService.updateTranscript(eq(1L), any(TranscriptUpdateRequest.class)))
                .thenThrow(ResourceNotFoundException.forTranscript(1L));

        mockMvc.perform(put("/api/meetings/1/transcript")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
