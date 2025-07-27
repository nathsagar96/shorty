package com.shorty.urls.dto;

import com.shorty.urls.UrlVisibility;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record UpdateUrlRequest(
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
        @Size(max = 2048, message = "URL too long")
        String originalUrl,
    UrlVisibility visibility,
    @Future(message = "Expiration date must be in the future") LocalDateTime expiresAt,
    @Min(value = -1, message = "Click limit must be -1 (unlimited) or positive")
        @Max(value = 1000000, message = "Click limit too high")
        Integer clickLimit) {}
