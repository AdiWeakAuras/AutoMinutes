package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.*;
import com.autominutes.backend.entity.Meeting;
import com.autominutes.backend.entity.MeetingAttendee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {TranscriptMapper.class, AttendeeMapper.class})
public interface MeetingMapper {

  @Mapping(target = "attendees", source = "meetingAttendees")
  MeetingDTO toDto(Meeting meeting);

  Meeting toEntity(MeetingCreateRequest request);

  // now if any field is null in the request it doesn't override the actual value in the entity
  @Mapping(target = "id", ignore = true)
  @org.mapstruct.BeanMapping(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromRequest(MeetingUpdateRequest request, @MappingTarget Meeting meeting);

  // extracts the attendee from every MeetingAttendee
  default List<AttendeeDTO> mapAttendees(List<MeetingAttendee> meetingAttendees) {
    return meetingAttendees.stream().map(ma -> toAttendeeDto(ma)).toList();
  }

  default AttendeeDTO toAttendeeDto(MeetingAttendee ma) {
    return new AttendeeDTO(
        ma.getAttendee().getId(),
        ma.getAttendee().getName(),
        ma.getAttendee().getEmail(),
        ma.getAttendee().getRole());
  }
}
