package com.autominutes.backend.service.impl;

import com.autominutes.backend.dto.AIResultDTO;
import com.autominutes.backend.dto.ProcessMeetingRequest;
import com.autominutes.backend.entity.ActionItem;
import com.autominutes.backend.entity.ActionItemStatus;
import com.autominutes.backend.entity.AIResult;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.ProcessingStatus;
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

@Service
@Transactional(readOnly = true)
public class AiProcessingServiceImpl implements AiProcessingService {

  private static final String DEFAULT_PROMPT_TEMPLATE_NAME = "default_summary";

  private static final String JSON_INSTRUCTIONS =
      """
              Respond ONLY with a single valid JSON object, with no extra commentary \
              and no markdown code fences, matching exactly this structure:
              {
                "summary": "string",
                "detailed_summary": "string",
                "decisions": "string",
                "follow_up_notes": "string",
                "action_items": [
                  {
                    "description": "string",
                    "proposed_assignee": "string or null",
                    "deadline": "YYYY-MM-DD or null",
                    "status": "OPEN, IN_PROGRESS, DONE, or UNKNOWN"
                  }
                ]
              }

              STRICT RULES — do not deviate:
              1. "deadline" MUST be either null or a date in the format YYYY-MM-DD (e.g. "2026-07-22"). \
              Never use free text like "after feedback" or "next week". \
              If no specific date is mentioned, use null.
              2. "status" MUST be exactly one of: OPEN, IN_PROGRESS, DONE, UNKNOWN. No other values.
              3. Do not add any fields not listed above.
              4. Do not wrap the JSON in markdown code fences.
              5. Extract ALL tasks mentioned or implied in the transcript, one action item per task. \\
                      Do not group multiple tasks into one item.
              """;

  private final MeetingRepository meetingRepository;
  private final PromptTemplateRepository promptTemplateRepository;
  private final AIResultRepository aiResultRepository;
  private final AIResultMapper aiResultMapper;
  private final LlmClient llmClient;
  private final ObjectMapper objectMapper;
  private final MeetingStatusUpdater meetingStatusUpdater;

  public AiProcessingServiceImpl(
      MeetingRepository meetingRepository,
      PromptTemplateRepository promptTemplateRepository,
      AIResultRepository aiResultRepository,
      AIResultMapper aiResultMapper,
      LlmClient llmClient,
      ObjectMapper objectMapper,
      MeetingStatusUpdater meetingStatusUpdater) {
    this.meetingRepository = meetingRepository;
    this.promptTemplateRepository = promptTemplateRepository;
    this.aiResultRepository = aiResultRepository;
    this.aiResultMapper = aiResultMapper;
    this.llmClient = llmClient;
    this.objectMapper = objectMapper;
    this.meetingStatusUpdater = meetingStatusUpdater;
  }

  @Override
  @Transactional
  public AIResultDTO processTranscript(Long meetingId, ProcessMeetingRequest request) {
    Meeting meeting =
        meetingRepository
            .findById(meetingId)
            .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

    Transcript transcript = meeting.getTranscript();
    if (transcript == null) {
      throw ResourceNotFoundException.forTranscript(meetingId);
    }
    if (transcript.getContent() == null || transcript.getContent().isBlank()) {
      throw InvalidRequestException.emptyTranscript(meetingId);
    }

    PromptTemplate promptTemplate = resolvePromptTemplate(request);
    String basePrompt =
        promptTemplate
            .getTemplateText()
            .replace("{transcript}", transcript.getContent())
            .replace("{current_date}", LocalDate.now().toString());
    String fullPrompt = basePrompt + "\n\n" + JSON_INSTRUCTIONS;

    try {
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

      meeting.setProcessingStatus(ProcessingStatus.DONE);
      meetingRepository.save(meeting);

      return aiResultMapper.toDto(saved);
    } catch (RuntimeException ex) {
      meetingStatusUpdater.markAsFailed(meetingId);
      throw ex;
    }
  }

  @Override
  public List<AIResultDTO> getAiResultsForMeeting(Long meetingId) {
    Meeting meeting =
        meetingRepository
            .findById(meetingId)
            .orElseThrow(() -> ResourceNotFoundException.forMeeting(meetingId));

    Transcript transcript = meeting.getTranscript();
    if (transcript == null) {
      return List.of();
    }
    return transcript.getAiResults().stream().map(aiResultMapper::toDto).toList();
  }

  private PromptTemplate resolvePromptTemplate(ProcessMeetingRequest request) {
    if (request != null && request.promptTemplateId() != null) {
      return promptTemplateRepository
          .findById(request.promptTemplateId())
          .orElseThrow(
              () -> ResourceNotFoundException.forPromptTemplate(request.promptTemplateId()));
    }
    return promptTemplateRepository
        .findByName(DEFAULT_PROMPT_TEMPLATE_NAME)
        .orElseThrow(
            () -> AiProcessingException.missingDefaultPromptTemplate(DEFAULT_PROMPT_TEMPLATE_NAME));
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

  private ActionItemStatus normalizeStatus(String rawStatus) {
    if (rawStatus == null || rawStatus.isBlank()) {
      return ActionItemStatus.UNKNOWN;
    }
    try {
      return ActionItemStatus.valueOf(rawStatus.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw AiProcessingException.invalidActionItemStatus(rawStatus);
    }
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
