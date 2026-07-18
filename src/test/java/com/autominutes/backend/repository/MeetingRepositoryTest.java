package com.autominutes.backend.repository;

import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.ProcessingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reuses the app's own configured H2 datasource (see application.properties) instead of
 * DataJpaTest's default embedded-replacement, so Flyway seeds the same test data
 * (V1.0.1__inserting_test_data.sql) that the rest of the app relies on.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MeetingRepositoryTest {

    @Autowired
    private MeetingRepository meetingRepository;

    @Test
    void findAllSummaries_returnsSeededMeetingsWithCorrectAttendeeCounts() {
        List<MeetingSummaryProjection> summaries = meetingRepository.findAllSummaries();

        assertThat(summaries).hasSize(2);

        MeetingSummaryProjection sprintPlanning = summaries.stream()
                .filter(s -> s.getTitle().equals("Sprint Planning"))
                .findFirst().orElseThrow();
        assertThat(sprintPlanning.getAttendeeCount()).isEqualTo(2L);
        assertThat(sprintPlanning.getProcessingStatus()).isEqualTo(ProcessingStatus.PENDING);

        MeetingSummaryProjection retro = summaries.stream()
                .filter(s -> s.getTitle().equals("Q2 Retrospective"))
                .findFirst().orElseThrow();
        assertThat(retro.getAttendeeCount()).isEqualTo(2L);
        assertThat(retro.getProcessingStatus()).isEqualTo(ProcessingStatus.DONE);
    }

    @Test
    void save_persistsNewMeetingWithDefaults() {
        Meeting meeting = new Meeting();
        meeting.setTitle("New Meeting");
        meeting.setMeetingDate(LocalDateTime.of(2026, 8, 1, 9, 0));

        Meeting saved = meetingRepository.save(meeting);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProcessingStatus()).isEqualTo(ProcessingStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();

        assertThat(meetingRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findById_returnsEmptyForUnknownId() {
        assertThat(meetingRepository.findById(9999L)).isEmpty();
    }
}
