package com.autominutes.backend.llm;


public interface LlmClient {

    String generateStructuredResult(String prompt);
}