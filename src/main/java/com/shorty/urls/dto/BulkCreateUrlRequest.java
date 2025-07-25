package com.shorty.urls.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BulkCreateUrlRequest(
    @NotEmpty(message = "URL list cannot be empty")
        @Size(max = 100, message = "Cannot create more than 100 URLs at once")
        @Valid
        List<CreateUrlRequest> urls) {}
