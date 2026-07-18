package com.autominutes.backend.controller;

import com.autominutes.backend.dto.AttendeeCreateRequest;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequest;
import com.autominutes.backend.exception.DuplicateResourceException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.service.AttendeeService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendeeController.class)
class AttendeeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private AttendeeService attendeeService;

    @Test
    void getAttendeesForMeeting_returnsList() throws Exception {
        AttendeeDTO dto = new AttendeeDTO(1L, "Ana", "ana@example.com", "PM");
        when(attendeeService.getAttendeesForMeeting(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/meetings/1/attendees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ana"));
    }

    @Test
    void getAttendeesForMeeting_returns404WhenMeetingMissing() throws Exception {
        when(attendeeService.getAttendeesForMeeting(99L))
                .thenThrow(ResourceNotFoundException.forMeeting(99L));

        mockMvc.perform(get("/api/meetings/99/attendees"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAttendeeById_returnsAttendee() throws Exception {
        AttendeeDTO dto = new AttendeeDTO(2L, "Radu", "radu@example.com", "DEV");
        when(attendeeService.getAttendeeForMeeting(1L, 2L)).thenReturn(dto);

        mockMvc.perform(get("/api/meetings/1/attendees/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("radu@example.com"));
    }

    @Test
    void addAttendee_returns201WhenValid() throws Exception {
        AttendeeCreateRequest request = new AttendeeCreateRequest("Maria", "maria@example.com", "STAKEHOLDER");
        AttendeeDTO dto = new AttendeeDTO(3L, "Maria", "maria@example.com", "STAKEHOLDER");
        when(attendeeService.addAttendeeToMeeting(eq(1L), any(AttendeeCreateRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/meetings/1/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void addAttendee_returns400WhenEmailInvalid() throws Exception {
        AttendeeCreateRequest request = new AttendeeCreateRequest("Maria", "not-an-email", "STAKEHOLDER");

        mockMvc.perform(post("/api/meetings/1/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAttendee_returns400WhenNameBlank() throws Exception {
        AttendeeCreateRequest request = new AttendeeCreateRequest("", "maria@example.com", "STAKEHOLDER");

        mockMvc.perform(post("/api/meetings/1/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAttendee_returns409WhenAlreadyLinked() throws Exception {
        AttendeeCreateRequest request = new AttendeeCreateRequest("Ana", "ana@example.com", "PM");
        when(attendeeService.addAttendeeToMeeting(eq(1L), any(AttendeeCreateRequest.class)))
                .thenThrow(DuplicateResourceException.forAttendeeAlreadyInMeeting("ana@example.com", 1L));

        mockMvc.perform(post("/api/meetings/1/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateAttendee_returns200() throws Exception {
        AttendeeUpdateRequest request = new AttendeeUpdateRequest("New name", null, null);
        AttendeeDTO dto = new AttendeeDTO(2L, "New name", "radu@example.com", "DEV");
        when(attendeeService.updateAttendee(eq(1L), eq(2L), any(AttendeeUpdateRequest.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/meetings/1/attendees/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    void removeAttendee_returns204() throws Exception {
        mockMvc.perform(delete("/api/meetings/1/attendees/2"))
                .andExpect(status().isNoContent());

        verify(attendeeService).removeAttendeeFromMeeting(1L, 2L);
    }

    @Test
    void removeAttendee_returns404WhenMissing() throws Exception {
        org.mockito.Mockito.doThrow(ResourceNotFoundException.forAttendee(2L))
                .when(attendeeService).removeAttendeeFromMeeting(1L, 2L);

        mockMvc.perform(delete("/api/meetings/1/attendees/2"))
                .andExpect(status().isNotFound());
    }
}
