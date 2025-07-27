package com.shorty.urls;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shorty.SpringSecurityConfiguration;
import com.shorty.urls.dto.*;
import com.shorty.users.User;
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
@AutoConfigureMockMvc
@WebMvcTest(controllers = BulkUrlController.class)
class BulkUrlControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired private MockMvc mockMvc;
  @MockitoBean private BulkUrlService bulkUrlService;
  private Url url;

  @BeforeEach
  void setUp() throws Exception {
    objectMapper.registerModule(new JavaTimeModule());

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .password("hashedPassword")
            .build();

    url =
        Url.builder()
            .id(UUID.randomUUID())
            .originalUrl("http://example.com")
            .shortCode("example")
            .user(user)
            .build();
  }

  @Test
  @WithUserDetails("test@example.com")
  void bulkCreateUrls_validRequest_createsUrls() throws Exception {
    // given
    List<CreateUrlRequest> urlRequests =
        List.of(
            new CreateUrlRequest(
                "http://example1.com",
                "example1",
                UrlVisibility.PUBLIC,
                LocalDateTime.now().plusDays(1),
                1),
            new CreateUrlRequest(
                "http://example2.com",
                "example2",
                UrlVisibility.PUBLIC,
                LocalDateTime.now().plusDays(1),
                1));
    BulkCreateUrlRequest bulkRequest = new BulkCreateUrlRequest(urlRequests);

    url.setShortCode("example1");
    UrlResponse urlResponse1 = UrlResponse.from(url, "http://localhost:8080");

    url.setShortCode("example2");
    UrlResponse urlResponse2 = UrlResponse.from(url, "http://localhost:8080");

    BulkOperationResponse<UrlResponse> serviceResponse =
        new BulkOperationResponse<>(List.of(urlResponse1, urlResponse2), List.of(), 2, 2, 0);

    when(bulkUrlService.bulkCreateUrls(
            any(BulkCreateUrlRequest.class), any(User.class), anyString()))
        .thenReturn(serviceResponse);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/urls/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.successCount").value(2))
        .andExpect(jsonPath("$.errorCount").value(0))
        .andExpect(jsonPath("$.successful[0].shortUrl").value("http://localhost:8080/example1"))
        .andExpect(jsonPath("$.successful[1].shortUrl").value("http://localhost:8080/example2"));

    // verify interaction
    verify(bulkUrlService)
        .bulkCreateUrls(any(BulkCreateUrlRequest.class), any(User.class), anyString());
  }

  @Test
  @WithUserDetails("test@example.com")
  void bulkCreateUrls_invalidRequest_returnsBadRequest() throws Exception {
    // given
    List<CreateUrlRequest> urlRequests =
        List.of(
            new CreateUrlRequest(
                "invalid-url",
                "example1",
                UrlVisibility.PUBLIC,
                LocalDateTime.now().plusDays(1),
                1));
    BulkCreateUrlRequest bulkRequest = new BulkCreateUrlRequest(urlRequests);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/urls/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest))
                .with(csrf()))
        .andExpect(status().isBadRequest());

    // verify no interaction with service
    verify(bulkUrlService, never())
        .bulkCreateUrls(any(BulkCreateUrlRequest.class), any(User.class), anyString());
  }

  @Test
  @WithUserDetails("test@example.com")
  void bulkDeleteUrls_validRequest_deletesUrls() throws Exception {
    // given
    List<UUID> urlIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    BulkDeleteRequest deleteRequest = new BulkDeleteRequest(urlIds);

    BulkOperationResponse<UUID> serviceResponse =
        new BulkOperationResponse<>(urlIds, List.of(), 2, 2, 0);

    when(bulkUrlService.bulkDeleteUrls(anyList(), any(UUID.class))).thenReturn(serviceResponse);

    // when & then
    mockMvc
        .perform(
            delete("/api/v1/urls/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteRequest))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.successCount").value(2))
        .andExpect(jsonPath("$.errorCount").value(0))
        .andExpect(jsonPath("$.successful[0]").value(urlIds.get(0).toString()))
        .andExpect(jsonPath("$.successful[1]").value(urlIds.get(1).toString()));

    // verify interaction
    verify(bulkUrlService).bulkDeleteUrls(anyList(), any(UUID.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void bulkUpdateVisibility_validRequest_updatesVisibility() throws Exception {
    // given
    List<UUID> urlIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    BulkUpdateVisibilityRequest updateRequest =
        new BulkUpdateVisibilityRequest(urlIds, UrlVisibility.PRIVATE);

    url.setVisibility(UrlVisibility.PRIVATE);
    UrlResponse urlResponse1 = UrlResponse.from(url, "http://localhost:8080");
    UrlResponse urlResponse2 = UrlResponse.from(url, "http://localhost:8080");

    BulkOperationResponse<UrlResponse> serviceResponse =
        new BulkOperationResponse<>(List.of(urlResponse1, urlResponse2), List.of(), 2, 2, 0);

    when(bulkUrlService.bulkUpdateVisibility(
            anyList(), any(UrlVisibility.class), any(UUID.class), anyString()))
        .thenReturn(serviceResponse);

    // when & then
    mockMvc
        .perform(
            put("/api/v1/urls/bulk/visibility")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.successCount").value(2))
        .andExpect(jsonPath("$.errorCount").value(0))
        .andExpect(jsonPath("$.successful[0].visibility").value("PRIVATE"))
        .andExpect(jsonPath("$.successful[1].visibility").value("PRIVATE"));

    // verify interaction
    verify(bulkUrlService)
        .bulkUpdateVisibility(anyList(), any(UrlVisibility.class), any(UUID.class), anyString());
  }

  @Test
  @WithUserDetails("test@example.com")
  void bulkToggleStatus_validRequest_togglesStatus() throws Exception {
    // given
    List<UUID> urlIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    BulkToggleStatusRequest toggleRequest = new BulkToggleStatusRequest(urlIds, true);

    url.setActive(true);
    UrlResponse urlResponse1 = UrlResponse.from(url, "http://localhost:8080");
    UrlResponse urlResponse2 = UrlResponse.from(url, "http://localhost:8080");

    BulkOperationResponse<UrlResponse> serviceResponse =
        new BulkOperationResponse<>(List.of(urlResponse1, urlResponse2), List.of(), 2, 2, 0);

    when(bulkUrlService.bulkToggleStatus(anyList(), anyBoolean(), any(UUID.class), anyString()))
        .thenReturn(serviceResponse);

    // when & then
    mockMvc
        .perform(
            put("/api/v1/urls/bulk/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toggleRequest))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.successCount").value(2))
        .andExpect(jsonPath("$.errorCount").value(0))
        .andExpect(jsonPath("$.successful[0].active").value(true))
        .andExpect(jsonPath("$.successful[1].active").value(true));

    // verify interaction
    verify(bulkUrlService).bulkToggleStatus(anyList(), anyBoolean(), any(UUID.class), anyString());
  }
}
