package com.shorty.urls.dto;

import com.shorty.urls.UrlVisibility;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record BulkUpdateVisibilityRequest(
    @NotEmpty(message = "URL ID list cannot be empty") List<UUID> urlIds,
    @NotNull(message = "Visibility cannot be null") UrlVisibility visibility) {}
