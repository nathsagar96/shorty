package com.shorty.urls.dto;

import com.shorty.urls.UrlVisibility;

public record UpdateUrlRequest(String originalUrl, UrlVisibility visibility) {}
