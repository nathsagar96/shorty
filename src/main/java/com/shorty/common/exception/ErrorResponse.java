package com.shorty.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(String code, String message, LocalDateTime timestamp) {}
