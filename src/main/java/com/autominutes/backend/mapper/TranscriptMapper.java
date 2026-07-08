package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.entity.Transcript;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TranscriptMapper {
    TranscriptDTO toDto(Transcript transcript);
}