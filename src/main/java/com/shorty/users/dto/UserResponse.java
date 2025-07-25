package com.shorty.users.dto;

import com.shorty.users.User;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String fullName,
    boolean enabled,
    LocalDateTime createdAt,
    int urlCount) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getFullName(),
        user.isEnabled(),
        user.getCreatedAt(),
        user.getUrls().size());
  }
}
