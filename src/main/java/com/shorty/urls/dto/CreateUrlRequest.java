package com.shorty.urls.dto;

import com.shorty.urls.UrlVisibility;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateUrlRequest(
    @NotBlank(message = "URL is required")
        @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
        @Size(max = 2048, message = "URL too long")
        String url,
    @Size(max = 50, message = "Custom code too long")
        @Pattern(
            regexp = "^[a-zA-Z0-9-_]*$",
            message = "Custom code can only contain letters, numbers, hyphens, and underscores")
        String customCode,
    UrlVisibility visibility,
    @Future(message = "Expiration date must be in the future") LocalDateTime expiresAt,
    @Min(value = 1, message = "Click limit must be positive")
        @Max(value = 1000000, message = "Click limit too high")
        Integer clickLimit,
    @Size(max = 500, message = "Description too long") String description,
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
        String password) {
  public CreateUrlRequest {
    if (visibility == null) {
      visibility = UrlVisibility.PUBLIC;
    }
    if (expiresAt == null) {
      expiresAt = LocalDateTime.now().plusDays(365);
    }
  }
}
