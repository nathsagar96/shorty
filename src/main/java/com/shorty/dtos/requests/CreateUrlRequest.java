package com.shorty.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateUrlRequest(
        @NotBlank(message = "URL cannot be blank")
                @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
                String url,
        String customAlias,
        @Positive(message = "Hours to expire must be a positive number") Integer hoursToExpire) {}
