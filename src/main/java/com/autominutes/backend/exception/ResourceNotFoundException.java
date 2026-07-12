package com.autominutes.backend.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forMeeting(Long id) {
        return new ResourceNotFoundException("Meeting with id " + id + " was not found.");
    }
    public static ResourceNotFoundException forAttendee(Long id) {
        return new ResourceNotFoundException("Attendee with id " + id + " was not found.");
    }
    public static ResourceNotFoundException forTranscript(Long id) {
        return new ResourceNotFoundException("Meeting with id" + id + " has no transcript.");
    }

}