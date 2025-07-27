package com.shorty.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shorty.users.dto.AuthenticationResponse;
import com.shorty.users.dto.UserLoginRequest;
import com.shorty.users.dto.UserRegistrationRequest;
import com.shorty.users.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthenticationService authenticationService;
  private AuthenticationResponse authenticationResponse;

  @BeforeEach
  void setUp() {
    // Common setup for creating a reusable AuthenticationResponse
    UserResponse userResponse =
        new UserResponse(
            UUID.randomUUID(),
            "test@example.com",
            "Test",
            "User",
            "Test User",
            true,
            LocalDateTime.now(),
            0);

    authenticationResponse = AuthenticationResponse.create("jwt-token-string", 3600L);
  }

  @Test
  void register_validRegistrationRequest_createsUser() throws Exception {
    // given
    UserRegistrationRequest validRequest =
        new UserRegistrationRequest("test@example.com", "Test", "User", "password123");
    when(authenticationService.register(any(UserRegistrationRequest.class)))
        .thenReturn(authenticationResponse);

    // when & then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.token").value("jwt-token-string"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"));

    // verify interaction
    verify(authenticationService).register(any(UserRegistrationRequest.class));
  }

  @Test
  void register_InvalidEmailRequest_returnsBadRequest() throws Exception {
    // given
    UserRegistrationRequest invalidRequest =
        new UserRegistrationRequest("not-an-email", "Test", "User", "password123");

    // when & then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    // verify no interaction with service
    verify(authenticationService, never()).register(any(UserRegistrationRequest.class));
  }

  @Test
  void register_shortPasswordRequest_returnsBadRequest() throws Exception {
    // given
    UserRegistrationRequest invalidRequest =
        new UserRegistrationRequest("test@example.com", "Test", "User", "pass");

    // when & then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    // verify no interaction with service
    verify(authenticationService, never()).register(any(UserRegistrationRequest.class));
  }

  @Test
  void login_validLoginRequest_returnsAuthenticationResponse() throws Exception {
    // given
    UserLoginRequest validRequest = new UserLoginRequest("test@example.com", "password123");
    when(authenticationService.authenticate(any(UserLoginRequest.class)))
        .thenReturn(authenticationResponse);

    // when & then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.token").value("jwt-token-string"));

    // verify interaction
    verify(authenticationService).authenticate(any(UserLoginRequest.class));
  }

  @Test
  void login_blankPasswordRequest_returnsBadRequest() throws Exception {
    // given
    UserLoginRequest invalidRequest = new UserLoginRequest("test@example.com", "");

    // when & then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    // verify no interaction with service
    verify(authenticationService, never()).authenticate(any(UserLoginRequest.class));
  }
}
