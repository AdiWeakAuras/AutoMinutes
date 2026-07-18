package com.autominutes.backend.repository;

import com.autominutes.backend.entity.MeetingAttendee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MeetingAttendeeRepositoryTest {

    @Autowired
    private MeetingAttendeeRepository meetingAttendeeRepository;

    @Test
    void findByMeetingId_returnsBothSeededLinksForSprintPlanning() {
        List<MeetingAttendee> links = meetingAttendeeRepository.findByMeetingId(1L);

        assertThat(links).hasSize(2);
        assertThat(links).extracting(ma -> ma.getAttendee().getId())
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void findByMeetingId_returnsEmptyForUnknownMeeting() {
        assertThat(meetingAttendeeRepository.findByMeetingId(9999L)).isEmpty();
    }

    @Test
    void findByMeetingIdAndAttendeeId_returnsLinkWhenPresent() {
        Optional<MeetingAttendee> link = meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 1L);

        assertThat(link).isPresent();
        assertThat(link.get().getMeeting().getId()).isEqualTo(1L);
        assertThat(link.get().getAttendee().getId()).isEqualTo(1L);
    }

    @Test
    void findByMeetingIdAndAttendeeId_returnsEmptyWhenNotLinked() {
        assertThat(meetingAttendeeRepository.findByMeetingIdAndAttendeeId(1L, 3L)).isEmpty();
    }
}
