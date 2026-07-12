package com.autominutes.backend.service;

import com.autominutes.backend.dto.AttendeeCreateRequest;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequest;

import java.util.List;

/**
 * Service for managing attendees associated with a meeting.
 * <p>
 * Covers requirement 3.2 of the backend specification (Attendee Management).
 * The meeting-attendee relationship is many-to-many: the same attendee (uniquely
 * identified by email) can be associated with multiple meetings, through the
 * {@code meeting_attendee} junction table.
 */
public interface AttendeeService {

    /**
     * Lists all attendees associated with a meeting.
     *
     * @param meetingId the id of the meeting
     * @return list of attendees, may be empty
     * @throws com.autominutes.backend.exception.ResourceNotFoundException if the meeting does not exist
     */
    List<AttendeeDTO> getAttendeesForMeeting(Long meetingId);

    /**
     * Returns a specific attendee, only if associated with the given meeting.
     *
     * @param meetingId  the id of the meeting
     * @param attendeeId the id of the attendee
     * @return the attendee DTO
     * @throws com.autominutes.backend.exception.ResourceNotFoundException if the meeting-attendee link does not exist
     */
    AttendeeDTO getAttendeeForMeeting(Long meetingId, Long attendeeId);

    /**
     * Adds an attendee to a meeting. If an attendee with the same email already exists
     * in the system (regardless of other meetings), it is reused (no duplicate is created);
     * only a new link is created in {@code meeting_attendee}.
     *
     * @param meetingId the id of the meeting the attendee is being added to
     * @param request   the attendee's data (name, email required, role)
     * @return the attendee (newly created or reused)
     * @throws com.autominutes.backend.exception.ResourceNotFoundException  if the meeting does not exist
     * @throws com.autominutes.backend.exception.DuplicateResourceException if the attendee is already linked to this meeting
     */
    AttendeeDTO addAttendeeToMeeting(Long meetingId, AttendeeCreateRequest request);

    /**
     * Partially updates an attendee's details. Fields left unset (null) remain unchanged.
     * The update affects the attendee globally (and therefore also their appearances
     * at other meetings).
     *
     * @param meetingId  the id of the meeting (used to validate the link)
     * @param attendeeId the id of the attendee to update
     * @param request    the fields to update
     * @return the updated attendee
     * @throws com.autominutes.backend.exception.ResourceNotFoundException if the meeting-attendee link does not exist
     */
    AttendeeDTO updateAttendee(Long meetingId, Long attendeeId, AttendeeUpdateRequest request);

    /**
     * Removes an attendee from a meeting (deletes only the link in {@code meeting_attendee}).
     * The {@code Attendee} entity itself is not deleted, and remains valid for other meetings.
     *
     * @param meetingId  the id of the meeting
     * @param attendeeId the id of the attendee to remove
     * @throws com.autominutes.backend.exception.ResourceNotFoundException if the meeting-attendee link does not exist
     */
    void removeAttendeeFromMeeting(Long meetingId, Long attendeeId);
}