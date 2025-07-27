package com.shorty.users.dto;

public record AuthenticationResponse(String token, String tokenType, long expiresIn) {
  public static AuthenticationResponse create(String token, long expiresIn) {
    return new AuthenticationResponse(token, "Bearer", expiresIn);
  }
}
