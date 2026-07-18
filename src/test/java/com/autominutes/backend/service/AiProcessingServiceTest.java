package com.autominutes.backend.service;

import com.autominutes.backend.dto.AIResultDTO;
import com.autominutes.backend.dto.ProcessMeetingRequest;
import com.autominutes.backend.entity.AIResult;
import com.autominutes.backend.entity.ActionItem;
import com.autominutes.backend.entity.ActionItemStatus;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.ProcessingStatus;
import com.autominutes.backend.entity.PromptTemplate;
import com.autominutes.backend.entity.Transcript;
import com.autominutes.backend.exception.AiProcessingException;
import com.autominutes.backend.exception.InvalidRequestException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.llm.LlmClient;
import com.autominutes.backend.llm.LlmCommunicationException;
import com.autominutes.backend.mapper.AIResultMapper;
import com.autominutes.backend.repository.AIResultRepository;
import com.autominutes.backend.repository.MeetingRepository;
import com.autominutes.backend.repository.PromptTemplateRepository;
import com.autominutes.backend.service.impl.AiProcessingServiceImpl;
import com.autominutes.backend.service.impl.MeetingStatusUpdater;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ObjectMapper is used as a real instance (not a mock) because the JSON parsing
 * behavior of {@code AiProcessingServiceImpl} is exactly what several of these
 * tests are verifying (valid JSON, malformed JSON, missing fields, etc.).
 */
@ExtendWith(MockitoExtension.class)
class AiProcessingServiceImplTest {

    @Mock private MeetingRepository meetingRepository;
    @Mock private PromptTemplateRepository promptTemplateRepository;
    @Mock private AIResultRepository aiResultRepository;
    @Mock private AIResultMapper aiResultMapper;
    @Mock private LlmClient llmClient;
    @Mock private MeetingStatusUpdater meetingStatusUpdater;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AiProcessingServiceImpl aiProcessingService;

    private Meeting meeting;
    private Transcript transcript;
    private PromptTemplate promptTemplate;

    @BeforeEach
    void setUp() {
        aiProcessingService = new AiProcessingServiceImpl(
                meetingRepository, promptTemplateRepository, aiResultRepository,
                aiResultMapper, llmClient, objectMapper, meetingStatusUpdater);

        meeting = new Meeting();
        transcript = new Transcript();
        transcript.setContent("Ana: let's begin. Radu: sounds good.");
        meeting.setTranscript(transcript);

        promptTemplate = new PromptTemplate();
        promptTemplate.setName("default_summary");
        promptTemplate.setTemplateText("Summarize: {transcript}");
    }

    @Test
    void processTranscript_throwsWhenMeetingMissing() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processTranscript_throwsWhenNoTranscript() {
        Meeting meetingWithoutTranscript = new Meeting();
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meetingWithoutTranscript));

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processTranscript_throwsWhenTranscriptBlank() {
        transcript.setContent("   ");
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void processTranscript_throwsWhenExplicitPromptTemplateNotFound() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                aiProcessingService.processTranscript(1L, new ProcessMeetingRequest(42L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processTranscript_throwsWhenNoDefaultPromptTemplateConfigured() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findByName("default_summary")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(AiProcessingException.class);
    }

    @Test
    void processTranscript_happyPath_savesResultAndMarksMeetingDone() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findByName("default_summary")).thenReturn(Optional.of(promptTemplate));

        String llmJson = """
                {
                  "summary": "Short summary",
                  "detailed_summary": "Longer summary",
                  "decisions": "Some decisions",
                  "follow_up_notes": "Some notes",
                  "action_items": [
                    { "description": "Configure staging", "proposed_assignee": "Radu",
                      "deadline": "2026-07-20", "status": "OPEN" }
                  ]
                }
                """;
        when(llmClient.generateStructuredResult(anyString())).thenReturn(llmJson);

        AIResult savedResult = new AIResult();
        when(aiResultRepository.save(any(AIResult.class))).thenReturn(savedResult);

        AIResultDTO dto = new AIResultDTO(1L, "Short summary", "Longer summary",
                "Some decisions", "Some notes", List.of());
        when(aiResultMapper.toDto(savedResult)).thenReturn(dto);

        AIResultDTO result = aiProcessingService.processTranscript(1L, null);

        assertThat(result).isEqualTo(dto);
        assertThat(meeting.getProcessingStatus()).isEqualTo(ProcessingStatus.DONE);
        verify(meetingRepository).save(meeting);
        verify(meetingStatusUpdater, never()).markAsFailed(any());

        var aiResultCaptor = org.mockito.ArgumentCaptor.forClass(AIResult.class);
        verify(aiResultRepository).save(aiResultCaptor.capture());
        AIResult capturedAiResult = aiResultCaptor.getValue();
        assertThat(capturedAiResult.getSummary()).isEqualTo("Short summary");
        assertThat(capturedAiResult.getActionItems()).hasSize(1);
        ActionItem actionItem = capturedAiResult.getActionItems().get(0);
        assertThat(actionItem.getDescription()).isEqualTo("Configure staging");
        assertThat(actionItem.getProposedAssignee()).isEqualTo("Radu");
        assertThat(actionItem.getDeadline()).isEqualTo(java.time.LocalDate.of(2026, 7, 20));
        assertThat(actionItem.getStatus()).isEqualTo(ActionItemStatus.OPEN);
    }

    @Test
    void processTranscript_llmCommunicationFailure_marksMeetingFailedAndRethrows() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findByName("default_summary")).thenReturn(Optional.of(promptTemplate));
        when(llmClient.generateStructuredResult(anyString()))
                .thenThrow(new LlmCommunicationException("Ollama unreachable"));

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(LlmCommunicationException.class);

        verify(meetingStatusUpdater).markAsFailed(1L);
        verify(aiResultRepository, never()).save(any());
    }

    @Test
    void processTranscript_invalidJsonResponse_wrapsAsAiProcessingExceptionAndMarksFailed() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findByName("default_summary")).thenReturn(Optional.of(promptTemplate));
        when(llmClient.generateStructuredResult(anyString())).thenReturn("not valid json at all");

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(AiProcessingException.class);

        verify(meetingStatusUpdater).markAsFailed(1L);
    }

    @Test
    void processTranscript_actionItemMissingDescription_throwsAndMarksFailed() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findByName("default_summary")).thenReturn(Optional.of(promptTemplate));

        String llmJson = """
                {
                  "summary": "s", "detailed_summary": "d", "decisions": "dec", "follow_up_notes": "f",
                  "action_items": [ { "description": "  ", "proposed_assignee": null,
                    "deadline": null, "status": null } ]
                }
                """;
        when(llmClient.generateStructuredResult(anyString())).thenReturn(llmJson);

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(AiProcessingException.class);

        verify(meetingStatusUpdater).markAsFailed(1L);
        verify(aiResultRepository, never()).save(any());
    }

    @Test
    void processTranscript_invalidDeadlineFormat_throwsAndMarksFailed() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findByName("default_summary")).thenReturn(Optional.of(promptTemplate));

        String llmJson = """
                {
                  "summary": "s", "detailed_summary": "d", "decisions": "dec", "follow_up_notes": "f",
                  "action_items": [ { "description": "Do the thing", "proposed_assignee": null,
                    "deadline": "next tuesday", "status": "OPEN" } ]
                }
                """;
        when(llmClient.generateStructuredResult(anyString())).thenReturn(llmJson);

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(AiProcessingException.class);

        verify(meetingStatusUpdater).markAsFailed(1L);
    }

    @Test
    void processTranscript_invalidActionItemStatus_throwsAndMarksFailed() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findByName("default_summary")).thenReturn(Optional.of(promptTemplate));

        String llmJson = """
                {
                  "summary": "s", "detailed_summary": "d", "decisions": "dec", "follow_up_notes": "f",
                  "action_items": [ { "description": "Do the thing", "proposed_assignee": null,
                    "deadline": null, "status": "NOT_A_REAL_STATUS" } ]
                }
                """;
        when(llmClient.generateStructuredResult(anyString())).thenReturn(llmJson);

        assertThatThrownBy(() -> aiProcessingService.processTranscript(1L, null))
                .isInstanceOf(AiProcessingException.class);

        verify(meetingStatusUpdater).markAsFailed(1L);
    }

    @Test
    void processTranscript_nullOrBlankStatus_normalizesToUnknown() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(promptTemplateRepository.findByName("default_summary")).thenReturn(Optional.of(promptTemplate));

        String llmJson = """
                {
                  "summary": "s", "detailed_summary": "d", "decisions": "dec", "follow_up_notes": "f",
                  "action_items": [ { "description": "Do the thing", "proposed_assignee": null,
                    "deadline": null, "status": "" } ]
                }
                """;
        when(llmClient.generateStructuredResult(anyString())).thenReturn(llmJson);
        when(aiResultRepository.save(any(AIResult.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aiResultMapper.toDto(any())).thenReturn(
                new AIResultDTO(1L, "s", "d", "dec", "f", List.of()));

        aiProcessingService.processTranscript(1L, null);

        var captor = org.mockito.ArgumentCaptor.forClass(AIResult.class);
        verify(aiResultRepository).save(captor.capture());
        assertThat(captor.getValue().getActionItems().get(0).getStatus())
                .isEqualTo(ActionItemStatus.UNKNOWN);
    }

    @Test
    void getAiResultsForMeeting_throwsWhenMeetingMissing() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiProcessingService.getAiResultsForMeeting(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAiResultsForMeeting_returnsEmptyListWhenNoTranscript() {
        Meeting meetingWithoutTranscript = new Meeting();
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meetingWithoutTranscript));

        assertThat(aiProcessingService.getAiResultsForMeeting(1L)).isEmpty();
    }

    @Test
    void getAiResultsForMeeting_mapsExistingResults() {
        AIResult aiResult = new AIResult();
        transcript.getAiResults().add(aiResult);
        AIResultDTO dto = new AIResultDTO(1L, "s", "d", "dec", "f", List.of());

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(aiResultMapper.toDto(aiResult)).thenReturn(dto);

        assertThat(aiProcessingService.getAiResultsForMeeting(1L)).containsExactly(dto);
    }
}