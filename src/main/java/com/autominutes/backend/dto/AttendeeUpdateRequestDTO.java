package com.autominutes.backend.dto;

import jakarta.validation.constraints.Email;

public record AttendeeUpdateRequestDTO(

        String name,

        @Email(message = "Email is not valid")
        String email,

        String role
) {}