package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.AIResultDTO;
import com.autominutes.backend.entity.ActionItem;
import com.autominutes.backend.entity.ActionItemStatus;
import com.autominutes.backend.entity.AIResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uses a narrow Spring context (just the mapper package) so that MapStruct's
 * generated {@code AIResultMapperImpl} gets its {@code uses = ActionItemMapper.class}
 * dependency wired the same way it would be in the real application context.
 */
@SpringJUnitConfig(AIResultMapperTest.MapperTestConfig.class)
class AIResultMapperTest {

    @Configuration
    @ComponentScan(basePackageClasses = AIResultMapper.class)
    static class MapperTestConfig {}

    @Autowired
    private AIResultMapper aiResultMapper;

    @Test
    void toDto_mapsFieldsAndNestedActionItems() {
        AIResult aiResult = new AIResult();
        aiResult.setId(1L);
        aiResult.setSummary("summary");
        aiResult.setDetailedSummary("detailed");
        aiResult.setDecisions("decisions");
        aiResult.setFollowUpNotes("notes");

        ActionItem item = new ActionItem();
        item.setId(10L);
        item.setDescription("Do the thing");
        item.setStatus(ActionItemStatus.OPEN);
        aiResult.getActionItems().add(item);

        AIResultDTO dto = aiResultMapper.toDto(aiResult);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.summary()).isEqualTo("summary");
        assertThat(dto.detailedSummary()).isEqualTo("detailed");
        assertThat(dto.decisions()).isEqualTo("decisions");
        assertThat(dto.followUpNotes()).isEqualTo("notes");
        assertThat(dto.actionItems()).hasSize(1);
        assertThat(dto.actionItems().get(0).description()).isEqualTo("Do the thing");
    }

    @Test
    void toDto_handlesEmptyActionItems() {
        AIResult aiResult = new AIResult();
        aiResult.setId(2L);
        aiResult.setSummary("s");

        AIResultDTO dto = aiResultMapper.toDto(aiResult);

        assertThat(dto.actionItems()).isEmpty();
    }
}
