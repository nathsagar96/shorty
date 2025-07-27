package com.shorty.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
    @NotBlank(message = "Email is required")
        @Size(max = 128, message = "Email must be maximum 128 characters")
        @Email(message = "Invalid email format")
        String email,
    @NotBlank(message = "First name is required")
        @Size(max = 64, message = "First name must be maximum 64 characters")
        String firstName,
    @Size(max = 64, message = "Last name must be between maximum 64 characters") String lastName,
    @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password) {}
