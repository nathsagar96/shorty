package com.shorty.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.shorty.common.exception.ValidationException;
import com.shorty.users.User;
import com.shorty.users.UserService;
import com.shorty.users.dto.AuthenticationResponse;
import com.shorty.users.dto.UserLoginRequest;
import com.shorty.users.dto.UserRegistrationRequest;
import com.shorty.users.dto.UserResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock private UserService userService;

  @Mock private JwtService jwtService;

  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthenticationService authenticationService;

  private UserRegistrationRequest registrationRequest;
  private UserLoginRequest loginRequest;
  private User user;
  private UserResponse userResponse;
  private String jwtToken;

  @BeforeEach
  void setUp() {
    registrationRequest =
        new UserRegistrationRequest("test@example.com", "Test", "User", "password");
    loginRequest = new UserLoginRequest("test@example.com", "password");
    user = new User("test@example.com", "Test", "User", "hashedPassword");
    userResponse =
        new UserResponse(
            UUID.randomUUID(), "test@example.com", "Test", "User", "Test User", true, null, 0);
    jwtToken = "test-jwt-token";
  }

  @Test
  void register_ShouldReturnAuthenticationResponse_WhenUserIsRegistered() {
    when(userService.registerUser(registrationRequest)).thenReturn(userResponse);
    when(userService.findById(any())).thenReturn(Optional.of(user));
    when(jwtService.generateToken(any())).thenReturn(jwtToken);
    when(jwtService.getExpirationTime()).thenReturn(3600L);

    AuthenticationResponse response = authenticationService.register(registrationRequest);

    assertNotNull(response);
    assertEquals(jwtToken, response.token());
    assertEquals(3600L, response.expiresIn());
    assertEquals(userResponse, response.user());
  }

  @Test
  void register_ShouldThrowException_WhenUserRegistrationFails() {
    when(userService.registerUser(registrationRequest))
        .thenThrow(new RuntimeException("Registration failed"));

    assertThrows(RuntimeException.class, () -> authenticationService.register(registrationRequest));
  }

  @Test
  void authenticate_ShouldReturnAuthenticationResponse_WhenUserIsAuthenticated() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(userService.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
    when(jwtService.generateToken(any())).thenReturn(jwtToken);
    when(jwtService.getExpirationTime()).thenReturn(3600L);

    AuthenticationResponse response = authenticationService.authenticate(loginRequest);

    assertNotNull(response);
    assertEquals(jwtToken, response.token());
    assertEquals(3600L, response.expiresIn());
    assertEquals(UserResponse.from(user), response.user());
  }

  @Test
  void authenticate_ShouldThrowValidationException_WhenAuthenticationFails() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new AuthenticationException("Invalid credentials") {});

    assertThrows(ValidationException.class, () -> authenticationService.authenticate(loginRequest));
  }

  @Test
  void authenticate_ShouldThrowValidationException_WhenUserNotFound() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(userService.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

    assertThrows(ValidationException.class, () -> authenticationService.authenticate(loginRequest));
  }
}
