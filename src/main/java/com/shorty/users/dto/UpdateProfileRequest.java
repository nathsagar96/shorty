package com.shorty.users.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 64, message = "First name must be maximum 64 characters") String firstName,
    @Size(max = 64, message = "Last name must be between maximum 64 characters") String lastName) {}
