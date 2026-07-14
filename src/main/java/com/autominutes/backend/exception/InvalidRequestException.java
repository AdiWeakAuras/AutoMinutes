package com.autominutes.backend.exception;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public static InvalidRequestException emptyTranscript(Long meetingId) {
        return new InvalidRequestException(
                "Meeting with id " + meetingId + " has a blank transcript; therefore cannot be proccesed");
    }
}