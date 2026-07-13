package com.autominutes.backend.llm;


public class LlmCommunicationException extends RuntimeException {

    public LlmCommunicationException(String message) {
        super(message);
    }

    public LlmCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}