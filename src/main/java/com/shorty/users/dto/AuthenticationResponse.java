package com.shorty.users.dto;

public record AuthenticationResponse(
    String token, String tokenType, long expiresIn, UserResponse user) {
  public static AuthenticationResponse create(String token, long expiresIn, UserResponse user) {
    return new AuthenticationResponse(token, "Bearer", expiresIn, user);
  }
}
