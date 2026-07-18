package com.autominutes.backend.controller;

import com.autominutes.backend.dto.AIResultDTO;
import com.autominutes.backend.dto.ActionItemDTO;
import com.autominutes.backend.dto.ProcessMeetingRequest;
import com.autominutes.backend.entity.ActionItemStatus;
import com.autominutes.backend.exception.AiProcessingException;
import com.autominutes.backend.exception.InvalidRequestException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.llm.LlmCommunicationException;
import com.autominutes.backend.service.AiProcessingService;
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

@WebMvcTest(AiProcessingController.class)
class AiProcessingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private AiProcessingService aiProcessingService;

    @Test
    void processTranscript_returns201WithBody() throws Exception {
        ProcessMeetingRequest request = new ProcessMeetingRequest(2L);
        AIResultDTO dto = new AIResultDTO(1L, "summary", "detailed", "decisions", "notes",
                List.of(new ActionItemDTO(1L, "Do the thing", "Ana", null, ActionItemStatus.OPEN)));
        when(aiProcessingService.processTranscript(eq(1L), any(ProcessMeetingRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/meetings/1/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary").value("summary"))
                .andExpect(jsonPath("$.actionItems[0].description").value("Do the thing"));
    }

    @Test
    void processTranscript_returns201WithoutBody() throws Exception {
        AIResultDTO dto = new AIResultDTO(1L, "summary", "detailed", "decisions", "notes", List.of());
        when(aiProcessingService.processTranscript(eq(1L), eq(null))).thenReturn(dto);

        mockMvc.perform(post("/api/meetings/1/process"))
                .andExpect(status().isCreated());
    }

    @Test
    void processTranscript_returns404WhenMeetingMissing() throws Exception {
        when(aiProcessingService.processTranscript(eq(99L), any()))
                .thenThrow(ResourceNotFoundException.forMeeting(99L));

        mockMvc.perform(post("/api/meetings/99/process"))
                .andExpect(status().isNotFound());
    }

    @Test
    void processTranscript_returns400WhenTranscriptEmpty() throws Exception {
        when(aiProcessingService.processTranscript(eq(1L), any()))
                .thenThrow(InvalidRequestException.emptyTranscript(1L));

        mockMvc.perform(post("/api/meetings/1/process"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTranscript_returns503WhenLlmUnreachable() throws Exception {
        when(aiProcessingService.processTranscript(eq(1L), any()))
                .thenThrow(new LlmCommunicationException("Ollama unreachable"));

        mockMvc.perform(post("/api/meetings/1/process"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void processTranscript_returns502WhenAiResponseInvalid() throws Exception {
        when(aiProcessingService.processTranscript(eq(1L), any()))
                .thenThrow(AiProcessingException.invalidResponseFormat(new RuntimeException("bad json")));

        mockMvc.perform(post("/api/meetings/1/process"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void getAiResults_returnsList() throws Exception {
        AIResultDTO dto = new AIResultDTO(1L, "summary", "detailed", "decisions", "notes", List.of());
        when(aiProcessingService.getAiResultsForMeeting(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/meetings/1/ai-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].summary").value("summary"));
    }

    @Test
    void getAiResults_returns404WhenMeetingMissing() throws Exception {
        when(aiProcessingService.getAiResultsForMeeting(99L))
                .thenThrow(ResourceNotFoundException.forMeeting(99L));

        mockMvc.perform(get("/api/meetings/99/ai-results"))
                .andExpect(status().isNotFound());
    }
}
