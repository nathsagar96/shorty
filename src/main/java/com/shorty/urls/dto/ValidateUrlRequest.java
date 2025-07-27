package com.shorty.urls.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateUrlRequest(@NotBlank(message = "URL is required") String url) {}
