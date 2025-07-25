package com.shorty.urls.dto;

public record BulkError(int index, String url, String errorMessage) {}
