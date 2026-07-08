package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.AIResultDTO;
import com.autominutes.backend.entity.AIResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = ActionItemMapper.class)
public interface AIResultMapper {
    AIResultDTO toDto(AIResult aiResult);
}