package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.MeetingAttendee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.autominutes.backend.dto.MeetingCreateRequest;
import com.autominutes.backend.dto.MeetingUpdateRequest;
import org.mapstruct.MappingTarget;


import java.util.List;

@Mapper(componentModel = "spring", uses = {TranscriptMapper.class, AIResultMapper.class, AttendeeMapper.class})
public interface MeetingMapper {

    @Mapping(target = "attendees", source = "meetingAttendees")
    MeetingDTO toDto(Meeting meeting);
    Meeting toEntity(MeetingCreateRequest request);
    // updates without creating a new instance, it will update the existing meeting entity with the values from the request
    void updateEntityFromRequest(MeetingUpdateRequest request, @MappingTarget Meeting meeting);

    // MapStruct generates automatically this implementantion, it iterates the list
    // and extracts the attendee from every MeetingAttendee
    default List<AttendeeDTO> mapAttendees(List<MeetingAttendee> meetingAttendees) {
        return meetingAttendees.stream()
                .map(ma -> toAttendeeDto(ma))
                .toList();
    }

    default AttendeeDTO toAttendeeDto(MeetingAttendee ma) {
        return new AttendeeDTO(
                ma.getAttendee().getId(),
                ma.getAttendee().getName(),
                ma.getAttendee().getEmail(),
                ma.getAttendee().getRole()
        );
    }
}