package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.TranscriptCreateRequestDTO;
import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.entity.AIResult;
import com.autominutes.backend.entity.Transcript;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(TranscriptMapperTest.MapperTestConfig.class)
class TranscriptMapperTest {

    @Configuration
    @ComponentScan(basePackageClasses = TranscriptMapper.class)
    static class MapperTestConfig {}

    @Autowired
    private TranscriptMapper transcriptMapper;

    @Test
    void toDto_mapsFieldsAndNestedAiResults() {
        Transcript transcript = new Transcript();
        transcript.setId(1L);
        transcript.setContent("Ana: hello");

        AIResult aiResult = new AIResult();
        aiResult.setId(5L);
        aiResult.setSummary("summary");
        transcript.getAiResults().add(aiResult);

        TranscriptDTO dto = transcriptMapper.toDto(transcript);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.content()).isEqualTo("Ana: hello");
        assertThat(dto.aiResults()).hasSize(1);
        assertThat(dto.aiResults().get(0).summary()).isEqualTo("summary");
    }

    @Test
    void toEntity_ignoresIdAndMeeting() {
        TranscriptCreateRequestDTO request = new TranscriptCreateRequestDTO("Some transcript content");

        Transcript entity = transcriptMapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getMeeting()).isNull();
        assertThat(entity.getContent()).isEqualTo("Some transcript content");
    }
}
