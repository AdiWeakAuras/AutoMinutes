package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.AttendeeCreateRequestDTO;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequestDTO;
import com.autominutes.backend.entity.Attendee;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AttendeeMapper {

    AttendeeDTO toDto(Attendee attendee);

    Attendee toEntity(AttendeeCreateRequestDTO request);

    // la fel ca la Meeting: daca un camp e null in request, nu suprascrie valoarea existenta
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AttendeeUpdateRequestDTO request, @MappingTarget Attendee attendee);
}