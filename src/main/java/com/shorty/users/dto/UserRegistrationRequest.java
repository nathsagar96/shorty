package com.shorty.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        String firstName,
    @Size(max = 100, message = "Last name must be between maximum 100 characters") String lastName,
    @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password) {}
