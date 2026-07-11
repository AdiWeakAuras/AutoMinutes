package com.autominutes.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AttendeeCreateRequest(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email is not valid")
        String email,

        String role
) {}