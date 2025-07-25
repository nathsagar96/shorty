package com.shorty.urls.dto;

import com.shorty.urls.UrlVisibility;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateUrlRequest(
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
        @Size(max = 2048, message = "URL too long")
        String originalUrl,
    UrlVisibility visibility,
    LocalDateTime expiresAt,
    @Min(value = -1, message = "Click limit must be -1 (unlimited) or positive")
        @Max(value = 1000000, message = "Click limit too high")
        Integer clickLimit,
    @Size(max = 500, message = "Description too long") String description,
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
        String password,
    Boolean removePassword) {}
