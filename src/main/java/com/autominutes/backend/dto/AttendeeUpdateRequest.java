package com.autominutes.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AttendeeUpdateRequest(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @Email(message = "Email is not valid")
        String email,

        String role
) {}