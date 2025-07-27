package com.shorty.urls.dto;

import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;

public record ExtendExpirationRequest(
    @Future(message = "New expiration date must be in the future")
        LocalDateTime newExpirationDate) {}
