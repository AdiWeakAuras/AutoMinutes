package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.entity.Attendee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendeeMapper {
    AttendeeDTO toDto(Attendee attendee);
}