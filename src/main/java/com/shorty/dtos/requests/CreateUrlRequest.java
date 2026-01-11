package com.shorty.dtos.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.time.Instant;

public record CreateUrlRequest(
        @NotBlank(message = "URL cannot be blank")
                @Size(max = 2048, message = "URL cannot exceed 2048 characters")
                @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
                String originalUrl,
        @Size(min = 3, max = 10, message = "Custom alias must be between 3 and 10 characters")
                @Pattern(
                        regexp = "^[a-zA-Z0-9_-]*$",
                        message = "Custom alias can only contain alphanumeric characters, hyphens, and underscores")
                @JsonInclude(JsonInclude.Include.NON_NULL)
                String customAlias,
        @Min(value = 1, message = "Expiration hours must be at least 1")
                @Max(value = 87600, message = "Expiration hours cannot exceed 10 years (87600 hours)")
                @JsonInclude(JsonInclude.Include.NON_NULL)
                Integer expirationHours) {
    public Instant calculateExpirationTime() {
        if (expirationHours == null) {
            return null;
        }
        return Instant.now().plusSeconds(expirationHours * 3600L);
    }
}
