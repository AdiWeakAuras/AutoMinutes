package com.autominutes.backend.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException forAttendeeEmail(String email) {
        return new DuplicateResourceException("Exista deja un attendee cu email-ul: " + email);
    }

    public static DuplicateResourceException forTranscript(Long meetingId) {
        return new DuplicateResourceException("Meet with id " + meetingId + " already has a transcript. Use put for updating.");
    }
}