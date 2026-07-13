package com.autominutes.backend.llm;

import com.autominutes.backend.config.LlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


import java.util.Map;


@Component
public class OllamaClient implements LlmClient {

    private final RestClient restClient;
    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    public OllamaClient(LlmProperties llmProperties, ObjectMapper objectMapper) {
        this.llmProperties = llmProperties;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = llmProperties.timeoutSeconds() * 1000;
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        this.restClient = RestClient.builder()
                .baseUrl(llmProperties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String generateStructuredResult(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", llmProperties.model(),
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        String rawResponse;
        try {
            rawResponse = restClient.post()
                    .uri("/api/generate")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw new LlmCommunicationException(
                    "Failed to reach Ollama at " + llmProperties.baseUrl() + ": " + ex.getMessage(), ex);
        }

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new LlmCommunicationException("Ollama returned an empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode responseField = root.get("response");
            if (responseField == null || responseField.isNull()) {
                throw new LlmCommunicationException(
                        "Ollama response did not contain the expected 'response' field: " + rawResponse);
            }
            return responseField.asText();
        } catch (LlmCommunicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LlmCommunicationException("Failed to parse Ollama's response envelope: " + ex.getMessage(), ex);
        }
    }
}