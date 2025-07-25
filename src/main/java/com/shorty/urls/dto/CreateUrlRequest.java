package com.shorty.urls.dto;

import com.shorty.urls.UrlVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
    UrlVisibility visibility) {
  public CreateUrlRequest {
    // Set default visibility if null
    if (visibility == null) {
      visibility = UrlVisibility.PUBLIC;
    }
  }
}
