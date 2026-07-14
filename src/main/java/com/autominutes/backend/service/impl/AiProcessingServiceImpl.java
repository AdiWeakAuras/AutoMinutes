package com.autominutes.backend.service.impl;

import com.autominutes.backend.dto.AIResultDTO;
import com.autominutes.backend.dto.ProcessMeetingRequest;
import com.autominutes.backend.entity.ActionItem;
import com.autominutes.backend.entity.AIResult;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.PromptTemplate;
import com.autominutes.backend.entity.Transcript;
import com.autominutes.backend.exception.AiProcessingException;
import com.autominutes.backend.exception.InvalidRequestException;
import com.autominutes.backend.exception.ResourceNotFoundException;
import com.autominutes.backend.llm.LlmClient;
import com.autominutes.backend.llm.dto.LlmActionItemResult;
import com.autominutes.backend.llm.dto.LlmProcessingResult;
import com.autominutes.backend.mapper.AIResultMapper;
import com.autominutes.backend.repository.AIResultRepository;
import com.autominutes.backend.repository.MeetingRepository;
import com.autominutes.backend.repository.PromptTemplateRepository;
import com.autominutes.backend.service.AiProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Standard implementation of {@link AiProcessingService}, delegating provider
 * communication to {@link LlmClient} and persistence to Spring Data JPA.
 */
@Service
@Transactional(readOnly = true)
public class AiProcessingServiceImpl implements AiProcessingService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("OPEN", "IN_PROGRESS", "DONE", "UNKNOWN");
    private static final String DEFAULT_PROMPT_TEMPLATE_NAME = "default_summary";

    private static final String JSON_INSTRUCTIONS = """
            Respond ONLY with a single valid JSON object, with no extra commentary \
            and no markdown code fences, matching exactly this structure:
            {
              "summary": "string",
              "detailed_summary": "string",
              "decisions": "string",
              "follow_up_notes": "string",
              "action_items": [
                { "description": "string", "proposed_assignee": "string or null", \
            "deadline": "YYYY-MM-DD or null", "status": "OPEN, IN_PROGRESS, DONE, or UNKNOWN" }
              ]
            }
            """;

    private final MeetingRepository meetingRepository;
    private final PromptTemplateRepository promptTemplateRepository;
    private final AIResultRepository aiResultRepository;
    private final AIResultMapper aiResultMapper;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public AiProcessingServiceImpl(MeetingRepository meetingRepository,
                                   PromptTemplateRepository promptTemplateRepository,
                                   AIResultRepository aiResultRepository,
                                   AIResultMapper aiResultMapper,
                                   LlmClient llmClient,
                                   ObjectMapper objectMapper) {
        this.meetingRepository = meetingRepository;
        this.promptTemplateRepository = promptTemplateRepository;
        this.aiResultRepository = aiResultRepository;
        this.aiResultMapper = aiResultMapper;
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AIResultDTO processTranscript(Long meetingId, ProcessMeetingRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

        Transcript transcript = meeting.getTranscript();
        if (transcript == null) {
            throw ResourceNotFoundException.forTranscript(meetingId);
        }
        if (transcript.getContent() == null || transcript.getContent().isBlank()) {
            throw InvalidRequestException.emptyTranscript(meetingId);
        }

        PromptTemplate promptTemplate = resolvePromptTemplate(request);

        String basePrompt = promptTemplate.getTemplateText().replace("{transcript}", transcript.getContent());
        String fullPrompt = basePrompt + "\n\n" + JSON_INSTRUCTIONS;

        String rawResponse = llmClient.generateStructuredResult(fullPrompt);
        LlmProcessingResult parsed = parseResponse(rawResponse);

        AIResult aiResult = new AIResult();
        aiResult.setTranscript(transcript);
        aiResult.setPromptTemplate(promptTemplate);
        aiResult.setSummary(parsed.summary());
        aiResult.setDetailedSummary(parsed.detailedSummary());
        aiResult.setDecisions(parsed.decisions());
        aiResult.setFollowUpNotes(parsed.followUpNotes());

        aiResult.getActionItems().addAll(buildActionItems(parsed, aiResult));

        AIResult saved = aiResultRepository.save(aiResult);

        meeting.setProcessingStatus("COMPLETED");
        meetingRepository.save(meeting);

        return aiResultMapper.toDto(saved);
    }

    @Override
    public List<AIResultDTO> getAiResultsForMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

        Transcript transcript = meeting.getTranscript();
        if (transcript == null) {
            return List.of();
        }
        return transcript.getAiResults().stream()
                .map(aiResultMapper::toDto)
                .toList();
    }

    private PromptTemplate resolvePromptTemplate(ProcessMeetingRequest request) {
        if (request != null && request.promptTemplateId() != null) {
            return promptTemplateRepository.findById(request.promptTemplateId())
                    .orElseThrow(() -> ResourceNotFoundException.forPromptTemplate(request.promptTemplateId()));
        }
        return promptTemplateRepository.findByName(DEFAULT_PROMPT_TEMPLATE_NAME)
                .orElseThrow(() -> new IllegalStateException(
                        "No default prompt template ('" + DEFAULT_PROMPT_TEMPLATE_NAME + "') found. "
                                + "Seed it via a Flyway migration or specify promptTemplateId explicitly."));
    }

    private LlmProcessingResult parseResponse(String rawResponse) {
        try {
            return objectMapper.readValue(rawResponse, LlmProcessingResult.class);
        } catch (Exception ex) {
            throw AiProcessingException.invalidResponseFormat(ex);
        }
    }

    private List<ActionItem> buildActionItems(LlmProcessingResult parsed, AIResult aiResult) {
        List<LlmActionItemResult> rawItems = parsed.actionItems();
        if (rawItems == null) {
            return List.of();
        }

        List<ActionItem> result = new ArrayList<>();
        for (LlmActionItemResult raw : rawItems) {
            if (raw.description() == null || raw.description().isBlank()) {
                throw AiProcessingException.missingActionItemDescription();
            }

            ActionItem item = new ActionItem();
            item.setAiResult(aiResult);
            item.setDescription(raw.description());
            item.setProposedAssignee(raw.proposedAssignee());
            item.setDeadline(parseDeadline(raw.deadline()));
            item.setStatus(normalizeStatus(raw.status()));
            result.add(item);
        }
        return result;
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "UNKNOWN";
        }
        String normalized = rawStatus.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw AiProcessingException.invalidActionItemStatus(rawStatus);
        }
        return normalized;
    }

    private LocalDate parseDeadline(String rawDeadline) {
        if (rawDeadline == null || rawDeadline.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(rawDeadline.trim());
        } catch (DateTimeParseException ex) {
            throw AiProcessingException.invalidDeadlineFormat(rawDeadline);
        }
    }
}