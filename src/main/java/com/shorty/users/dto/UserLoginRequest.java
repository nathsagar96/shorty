package com.shorty.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotBlank(message = "Password is required") String password) {}
