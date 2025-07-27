package com.shorty.urls.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record BulkToggleStatusRequest(
    @NotEmpty(message = "URL ID list cannot be empty") List<UUID> urlIds,
    @NotNull(message = "Status cannot be null") boolean active) {}
