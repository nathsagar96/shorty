package com.shorty.users;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shorty.SpringSecurityConfiguration;
import com.shorty.urls.Url;
import com.shorty.urls.UrlService;
import com.shorty.users.dto.ChangePasswordRequest;
import com.shorty.users.dto.UpdateProfileRequest;
import com.shorty.users.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(SpringSecurityConfiguration.class)
@AutoConfigureMockMvc()
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserService userService;
  @MockitoBean private UrlService urlService;
  private UserResponse userResponse;

  @BeforeEach
  void setUp() {
    // Common setup for creating a reusable UserResponse
    userResponse =
        new UserResponse(
            UUID.randomUUID(),
            "test@example.com",
            "Test",
            "User",
            "Test User",
            true,
            LocalDateTime.now(),
            0);
  }

  @Test
  @WithUserDetails("test@example.com")
  void getCurrentUserProfile_returnsUserProfile() throws Exception {
    // given
    when(userService.getUserProfile(any(UUID.class))).thenReturn(userResponse);

    // when & then
    mockMvc
        .perform(get("/api/v1/users/profile").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.firstName").value("Test"))
        .andExpect(jsonPath("$.lastName").value("User"));

    // verify interaction
    verify(userService).getUserProfile(any(UUID.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void getCurrentUserUrls_returnsUserUrls() throws Exception {
    // given
    when(urlService.getUserUrls(any(UUID.class)))
        .thenReturn(
            List.of(
                new Url("https://example.com", "Example"),
                new Url("https://example.org", "Example")));

    // when & then
    mockMvc
        .perform(get("/api/v1/users/urls").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(2));

    // verify interaction
    verify(urlService).getUserUrls(any(UUID.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void updateProfile_validRequest_updatesUserProfile() throws Exception {
    // given
    UpdateProfileRequest validRequest = new UpdateProfileRequest("Test", "User");
    when(userService.updateUserProfile(any(UUID.class), any(String.class), any(String.class)))
        .thenReturn(userResponse);

    // when & then
    mockMvc
        .perform(
            put("/api/v1/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.firstName").value("Test"))
        .andExpect(jsonPath("$.lastName").value("User"));

    // verify interaction
    verify(userService).updateUserProfile(any(UUID.class), any(String.class), any(String.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void changePassword_validRequest_changesPassword() throws Exception {
    // given
    ChangePasswordRequest validRequest = new ChangePasswordRequest("oldPassword", "newPassword");

    // when & then
    mockMvc
        .perform(
            post("/api/v1/users/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk());

    // verify interaction
    verify(userService).changePassword(any(UUID.class), any(String.class), any(String.class));
  }
}
