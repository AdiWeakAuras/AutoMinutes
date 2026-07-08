package com.autominutes.backend.dto;

public record AttendeeDTO(
        Long id,
        String name,
        String email,
        String role
) {}