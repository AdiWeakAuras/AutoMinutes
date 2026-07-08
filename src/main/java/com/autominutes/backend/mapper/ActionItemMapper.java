package com.autominutes.backend.mapper;

import com.autominutes.backend.dto.ActionItemDTO;
import com.autominutes.backend.entity.ActionItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActionItemMapper {
    ActionItemDTO toDto(ActionItem actionItem);
}