package com.autominutes.backend.exception;


public class AiProcessingException extends RuntimeException {

    public AiProcessingException(String message) {
        super(message);
    }

    public AiProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AiProcessingException invalidResponseFormat(Throwable cause) {
        return new AiProcessingException(
                "The AI model's response could not be parsed as the expected JSON structure", cause);
    }

    public static AiProcessingException invalidActionItemStatus(String status) {
        return new AiProcessingException(
                "The AI model returned an invalid action item status: '" + status
                        + "'. Allowed values are OPEN, IN_PROGRESS, DONE, UNKNOWN.");
    }

    public static AiProcessingException invalidDeadlineFormat(String deadline) {
        return new AiProcessingException(
                "The AI model returned a deadline that is not a valid date: '" + deadline + "'");
    }

    public static AiProcessingException missingActionItemDescription() {
        return new AiProcessingException("The AI model returned an action item without a description");
    }
}