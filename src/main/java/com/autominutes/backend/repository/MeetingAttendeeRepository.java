package com.autominutes.backend.repository;

import com.autominutes.backend.entity.MeetingAttendee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee, Long> {

    List<MeetingAttendee> findByMeetingId(Long meetingId);

    Optional<MeetingAttendee> findByMeetingIdAndAttendeeId(Long meetingId, Long attendeeId);
}