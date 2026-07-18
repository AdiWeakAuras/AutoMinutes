package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.AttendeeCreateRequest;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequest;
import com.autominutes.backend.entity.Attendee;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttendeeMapperTest {

    private final AttendeeMapper mapper = new AttendeeMapperImpl();

    @Test
    void toDto_mapsAllFields() {
        Attendee attendee = new Attendee();
        attendee.setId(1L);
        attendee.setName("Ana Popescu");
        attendee.setEmail("ana.popescu@example.com");
        attendee.setRole("PROJECT_MANAGER");

        AttendeeDTO dto = mapper.toDto(attendee);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Ana Popescu");
        assertThat(dto.email()).isEqualTo("ana.popescu@example.com");
        assertThat(dto.role()).isEqualTo("PROJECT_MANAGER");
    }

    @Test
    void toEntity_mapsRequestFieldsAndLeavesIdNull() {
        AttendeeCreateRequest request = new AttendeeCreateRequest("Radu", "radu@example.com", "DEVELOPER");

        Attendee entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Radu");
        assertThat(entity.getEmail()).isEqualTo("radu@example.com");
        assertThat(entity.getRole()).isEqualTo("DEVELOPER");
    }

    @Test
    void updateEntityFromRequest_overwritesOnlyNonNullFields() {
        Attendee attendee = new Attendee();
        attendee.setId(5L);
        attendee.setName("Old name");
        attendee.setEmail("old@example.com");
        attendee.setRole("OLD_ROLE");

        AttendeeUpdateRequest request = new AttendeeUpdateRequest("New name", null, null);

        mapper.updateEntityFromRequest(request, attendee);

        assertThat(attendee.getId()).isEqualTo(5L);
        assertThat(attendee.getName()).isEqualTo("New name");
        assertThat(attendee.getEmail()).isEqualTo("old@example.com");
        assertThat(attendee.getRole()).isEqualTo("OLD_ROLE");
    }

    @Test
    void updateEntityFromRequest_doesNotOverwriteIdEvenIfSomehowProvided() {
        Attendee attendee = new Attendee();
        attendee.setId(5L);
        attendee.setName("Name");

        AttendeeUpdateRequest request = new AttendeeUpdateRequest("Name", "new@example.com", "ROLE");

        mapper.updateEntityFromRequest(request, attendee);

        assertThat(attendee.getId()).isEqualTo(5L);
        assertThat(attendee.getEmail()).isEqualTo("new@example.com");
        assertThat(attendee.getRole()).isEqualTo("ROLE");
    }
}
