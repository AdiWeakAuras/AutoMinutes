package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.MeetingCreateRequestDTO;
import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.dto.MeetingUpdateRequestDTO;
import com.autominutes.backend.entity.Attendee;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.MeetingAttendee;
import com.autominutes.backend.entity.ProcessingStatus;
import com.autominutes.backend.entity.Transcript;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(MeetingMapperTest.MapperTestConfig.class)
class MeetingMapperTest {

    @Configuration
    @ComponentScan(basePackageClasses = MeetingMapper.class)
    static class MapperTestConfig {}

    @Autowired
    private MeetingMapper meetingMapper;

    @Test
    void toDto_mapsFieldsAndLinkedAttendees() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("Sprint Planning");
        meeting.setDescription("desc");
        meeting.setMeetingDate(LocalDateTime.of(2026, 7, 1, 10, 0));
        meeting.setProcessingStatus(ProcessingStatus.PENDING);

        Attendee attendee = new Attendee();
        attendee.setId(1L);
        attendee.setName("Ana");
        attendee.setEmail("ana@example.com");
        attendee.setRole("PM");

        MeetingAttendee link = new MeetingAttendee();
        link.setMeeting(meeting);
        link.setAttendee(attendee);
        meeting.getMeetingAttendees().add(link);

        MeetingDTO dto = meetingMapper.toDto(meeting);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("Sprint Planning");
        assertThat(dto.processingStatus()).isEqualTo(ProcessingStatus.PENDING);
        assertThat(dto.attendees()).hasSize(1);
        assertThat(dto.attendees().get(0).name()).isEqualTo("Ana");
        assertThat(dto.transcript()).isNull();
    }

    @Test
    void toDto_mapsNestedTranscriptWhenPresent() {
        Meeting meeting = new Meeting();
        meeting.setId(2L);
        meeting.setTitle("Q2 Retro");
        meeting.setMeetingDate(LocalDateTime.now());
        meeting.setProcessingStatus(ProcessingStatus.DONE);

        Transcript transcript = new Transcript();
        transcript.setId(9L);
        transcript.setContent("content");
        meeting.setTranscript(transcript);

        MeetingDTO dto = meetingMapper.toDto(meeting);

        assertThat(dto.transcript()).isNotNull();
        assertThat(dto.transcript().content()).isEqualTo("content");
    }

    @Test
    void toEntity_mapsCreateRequestFields() {
        MeetingCreateRequestDTO request = new MeetingCreateRequestDTO("Title", "Desc",
                LocalDateTime.of(2026, 8, 1, 9, 0));

        Meeting entity = meetingMapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getTitle()).isEqualTo("Title");
        assertThat(entity.getDescription()).isEqualTo("Desc");
        assertThat(entity.getMeetingDate()).isEqualTo(LocalDateTime.of(2026, 8, 1, 9, 0));
    }

    @Test
    void updateEntityFromRequest_patchesOnlyProvidedFields() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("Old title");
        meeting.setDescription("Old desc");
        meeting.setMeetingDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        meeting.setProcessingStatus(ProcessingStatus.PENDING);

        MeetingUpdateRequestDTO request = new MeetingUpdateRequestDTO("New title", null, null, ProcessingStatus.DONE);

        meetingMapper.updateEntityFromRequest(request, meeting);

        assertThat(meeting.getId()).isEqualTo(1L);
        assertThat(meeting.getTitle()).isEqualTo("New title");
        assertThat(meeting.getDescription()).isEqualTo("Old desc");
        assertThat(meeting.getMeetingDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(meeting.getProcessingStatus()).isEqualTo(ProcessingStatus.DONE);
    }
}
