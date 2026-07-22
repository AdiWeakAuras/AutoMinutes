package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.TranscriptCreateRequest;
import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.entity.Transcript;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = AIResultMapper.class)
public interface TranscriptMapper {
    TranscriptDTO toDto(Transcript transcript);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meeting", ignore = true)
    Transcript toEntity(TranscriptCreateRequest request);
}