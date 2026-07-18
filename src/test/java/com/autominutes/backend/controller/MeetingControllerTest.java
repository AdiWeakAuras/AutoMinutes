package com.autominutes.backend.controller;

import com.autominutes.backend.dto.MeetingCreateRequest;
import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.dto.MeetingSummaryDTO;
import com.autominutes.backend.dto.MeetingUpdateRequest;
import com.autominutes.backend.entity.ProcessingStatus;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.service.MeetingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private MeetingService meetingService;

    @Test
    void getAllMeetings_returnsList() throws Exception {
        MeetingSummaryDTO summary = new MeetingSummaryDTO(1L, "Sprint Planning",
                LocalDateTime.of(2026, 7, 1, 10, 0), ProcessingStatus.PENDING, 2);
        when(meetingService.getAllMeetings()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/meetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Sprint Planning"))
                .andExpect(jsonPath("$[0].attendeeCount").value(2));
    }

    @Test
    void getMeetingById_returns200WhenFound() throws Exception {
        MeetingDTO dto = new MeetingDTO(1L, "Sprint Planning", "desc",
                LocalDateTime.now(), ProcessingStatus.PENDING, null, List.of());
        when(meetingService.getMeetingById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/meetings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sprint Planning"));
    }

    @Test
    void getMeetingById_returns404WhenMissing() throws Exception {
        when(meetingService.getMeetingById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/meetings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createMeeting_returns201WhenValid() throws Exception {
        MeetingCreateRequest request = new MeetingCreateRequest("Title", "Desc", LocalDateTime.now());
        MeetingDTO dto = new MeetingDTO(1L, "Title", "Desc", LocalDateTime.now(),
                ProcessingStatus.PENDING, null, List.of());
        when(meetingService.createMeeting(any(MeetingCreateRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createMeeting_returns400WhenTitleBlank() throws Exception {
        MeetingCreateRequest request = new MeetingCreateRequest("", "Desc", LocalDateTime.now());

        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMeeting_returns400WhenDateMissing() throws Exception {
        MeetingCreateRequest request = new MeetingCreateRequest("Title", "Desc", null);

        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMeeting_returns200() throws Exception {
        MeetingUpdateRequest request = new MeetingUpdateRequest("New title", null, null, null);
        MeetingDTO dto = new MeetingDTO(1L, "New title", "Desc", LocalDateTime.now(),
                ProcessingStatus.PENDING, null, List.of());
        when(meetingService.updateMeeting(eq(1L), any(MeetingUpdateRequest.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/meetings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"));
    }

    @Test
    void updateMeeting_returns404WhenServiceThrows() throws Exception {
        MeetingUpdateRequest request = new MeetingUpdateRequest("New title", null, null, null);
        when(meetingService.updateMeeting(eq(99L), any(MeetingUpdateRequest.class)))
                .thenThrow(ResourceNotFoundException.forMeeting(99L));

        mockMvc.perform(patch("/api/meetings/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMeeting_returns204() throws Exception {
        mockMvc.perform(delete("/api/meetings/1"))
                .andExpect(status().isNoContent());

        verify(meetingService).deleteMeeting(1L);
    }

    @Test
    void deleteMeeting_returns404WhenMissing() throws Exception {
        org.mockito.Mockito.doThrow(ResourceNotFoundException.forMeeting(99L))
                .when(meetingService).deleteMeeting(99L);

        mockMvc.perform(delete("/api/meetings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMeetingById_returns400OnNonNumericId() throws Exception {
        mockMvc.perform(get("/api/meetings/not-a-number"))
                .andExpect(status().isBadRequest());
    }
}
