package com.autominutes.backend.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException forAttendeeAlreadyInMeeting(String email, Long meetingId) {
        return new DuplicateResourceException(
                "Attendee with email-ul " + email + " is already linked to the meeting with id " + meetingId);
    }

    public static DuplicateResourceException forTranscript(Long meetingId) {
        return new DuplicateResourceException("Meet with id " + meetingId + " already has a transcript. Use put for updating.");
    }
}