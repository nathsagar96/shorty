package com.shorty.dtos.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.Instant;

public record CreateUrlRequest(
        @Schema(
                        description = "The original URL to be shortened",
                        example = "https://www.example.com",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "URL cannot be blank")
                @Size(max = 2048, message = "URL cannot exceed 2048 characters")
                @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
                String originalUrl,
        @Schema(
                        description = "Optional custom alias for the shortened URL",
                        example = "abc123",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(min = 3, max = 10, message = "Custom alias must be between 3 and 10 characters")
                @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Custom alias can only contain alphanumeric characters")
                @JsonInclude(JsonInclude.Include.NON_NULL)
                String customAlias,
        @Schema(
                        description = "Optional expiration time in hours",
                        example = "24",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
