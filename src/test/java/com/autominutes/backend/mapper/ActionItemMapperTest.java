package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.ActionItemDTO;
import com.autominutes.backend.entity.ActionItem;
import com.autominutes.backend.entity.ActionItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ActionItemMapperTest {

    private final ActionItemMapper mapper = new ActionItemMapperImpl();

    @Test
    void toDto_mapsAllFields() {
        ActionItem item = new ActionItem();
        item.setId(1L);
        item.setDescription("Configure staging");
        item.setProposedAssignee("Radu");
        item.setDeadline(LocalDate.of(2026, 7, 20));
        item.setStatus(ActionItemStatus.IN_PROGRESS);

        ActionItemDTO dto = mapper.toDto(item);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.description()).isEqualTo("Configure staging");
        assertThat(dto.proposedAssignee()).isEqualTo("Radu");
        assertThat(dto.deadline()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(dto.status()).isEqualTo(ActionItemStatus.IN_PROGRESS);
    }

    @Test
    void toDto_handlesNullOptionalFields() {
        ActionItem item = new ActionItem();
        item.setId(2L);
        item.setDescription("Do the thing");
        item.setProposedAssignee(null);
        item.setDeadline(null);
        item.setStatus(ActionItemStatus.UNKNOWN);

        ActionItemDTO dto = mapper.toDto(item);

        assertThat(dto.proposedAssignee()).isNull();
        assertThat(dto.deadline()).isNull();
        assertThat(dto.status()).isEqualTo(ActionItemStatus.UNKNOWN);
    }
}
