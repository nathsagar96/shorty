package com.shorty.urls;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shorty.SpringSecurityConfiguration;
import com.shorty.common.validation.UrlValidationService;
import com.shorty.urls.dto.CreateUrlRequest;
import com.shorty.urls.dto.UpdateUrlRequest;
import com.shorty.users.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SpringSecurityConfiguration.class)
@AutoConfigureMockMvc
@WebMvcTest(controllers = UrlController.class)
class UrlControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  UrlValidationService.ValidationResult validResult;
  UrlValidationService.ValidationResult invalidResult;
  @Autowired private MockMvc mockMvc;
  @MockitoBean private UrlService urlService;
  @MockitoBean private UrlValidationService validationService;
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

    validResult = new UrlValidationService.ValidationResult();
    invalidResult = new UrlValidationService.ValidationResult();
    invalidResult.addError("Invalid URL");

    when(validationService.validateUrl(anyString())).thenReturn(validResult);
    when(validationService.validateCustomCode(anyString())).thenReturn(validResult);
    when(validationService.validateExpirationDate(any(LocalDateTime.class)))
        .thenReturn(validResult);
  }

  @Test
  @WithUserDetails("test@example.com")
  void createShortUrl_validRequest_createsUrl() throws Exception {
    // given
    CreateUrlRequest validRequest =
        new CreateUrlRequest(
            "http://example.com",
            "example",
            UrlVisibility.PUBLIC,
            LocalDateTime.now().plusDays(1),
            1);
    when(urlService.createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class)))
        .thenReturn(url);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/example"))
        .andExpect(jsonPath("$.originalUrl").value("http://example.com"));

    // verify interaction
    verify(urlService)
        .createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void createShortUrl_invalidRequest_returnsBadRequest() throws Exception {
    // given
    CreateUrlRequest invalidRequest =
        new CreateUrlRequest(
            "http://example.com",
            "example",
            UrlVisibility.PUBLIC,
            LocalDateTime.now().plusDays(1),
            1);

    when(validationService.validateUrl(anyString())).thenReturn(invalidResult);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
        .andExpect(status().isBadRequest());

    // verify no interaction with service
    verify(urlService, never())
        .createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void getUserUrls_returnsUserUrls() throws Exception {
    // given
    List<Url> urls = List.of(url);
    when(urlService.getUserUrls(any(UUID.class))).thenReturn(urls);

    // when & then
    mockMvc
        .perform(get("/api/v1/urls").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].shortUrl").value("http://localhost:8080/example"))
        .andExpect(jsonPath("$[0].originalUrl").value("http://example.com"));

    // verify interaction
    verify(urlService).getUserUrls(any(UUID.class));
  }

  @Test
  @WithMockUser
  void getPublicUrls_returnsPublicUrls() throws Exception {
    // given
    Page<Url> urls = new PageImpl<>(List.of(url));
    when(urlService.getPublicUrls(any(Pageable.class))).thenReturn(urls);

    // when & then
    mockMvc
        .perform(get("/api/v1/urls/public").param("page", "0").param("size", "20").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].shortUrl").value("http://localhost:8080/example"))
        .andExpect(jsonPath("$.content[0].originalUrl").value("http://example.com"));

    // verify interaction
    verify(urlService).getPublicUrls(any(Pageable.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void updateUrl_validRequest_updatesUrl() throws Exception {
    // given
    UpdateUrlRequest validRequest =
        new UpdateUrlRequest(
            "http://example.com", UrlVisibility.PRIVATE, LocalDateTime.now().plusDays(1), 1);
    url.setVisibility(UrlVisibility.PRIVATE);
    when(urlService.updateUrl(
            any(UUID.class),
            any(UUID.class),
            any(String.class),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt()))
        .thenReturn(url);

    // when & then
    mockMvc
        .perform(
            put("/api/v1/urls/{urlId}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/example"))
        .andExpect(jsonPath("$.originalUrl").value("http://example.com"))
        .andExpect(jsonPath("$.visibility").value(UrlVisibility.PRIVATE.name()));

    // verify interaction
    verify(urlService)
        .updateUrl(
            any(UUID.class),
            any(UUID.class),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt());
  }

  @Test
  @WithUserDetails("test@example.com")
  void toggleUrlStatus_togglesUrlStatus() throws Exception {
    // given
    url.setVisibility(UrlVisibility.PRIVATE);
    when(urlService.toggleUrlStatus(any(UUID.class), any(UUID.class))).thenReturn(url);

    // when & then
    mockMvc
        .perform(post("/api/v1/urls/{urlId}/toggle", UUID.randomUUID()).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/example"))
        .andExpect(jsonPath("$.originalUrl").value("http://example.com"))
        .andExpect(jsonPath("$.visibility").value(UrlVisibility.PRIVATE.name()));

    // verify interaction
    verify(urlService).toggleUrlStatus(any(UUID.class), any(UUID.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void deleteUrl_deletesUrl() throws Exception {
    // when & then
    mockMvc
        .perform(delete("/api/v1/urls/{urlId}", UUID.randomUUID()).with(csrf()))
        .andExpect(status().isNoContent());

    // verify interaction
    verify(urlService).deleteUrl(any(UUID.class), any(UUID.class));
  }

  @Test
  @WithMockUser
  void getUrlCount_returnsUrlCount() throws Exception {
    // given
    when(urlService.getUrlCount()).thenReturn(1L);

    // when & then
    mockMvc
        .perform(get("/api/v1/urls/count").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").value(1L));

    // verify interaction
    verify(urlService).getUrlCount();
  }

  @Test
  @WithUserDetails("test@example.com")
  void createShortUrl_WithExpiration_CreatesSuccessfully() throws Exception {
    // Given
    CreateUrlRequest request =
        new CreateUrlRequest(
            "http://example.com",
            "example",
            UrlVisibility.PUBLIC,
            LocalDateTime.now().plusDays(7),
            1);
    url.setShortCode("expiring123");
    url.setExpiresAt(LocalDateTime.now().plusDays(7));
    when(urlService.createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class)))
        .thenReturn(url);

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shortCode").value("expiring123"))
        .andExpect(jsonPath("$.expiresAt").isNotEmpty())
        .andExpect(jsonPath("$.expired").value(false));
  }

  @Test
  @WithUserDetails("test@example.com")
  void createShortUrl_WithClickLimit_CreatesSuccessfully() throws Exception {
    // Given
    CreateUrlRequest request =
        new CreateUrlRequest(
            "http://example.com",
            "example",
            UrlVisibility.PUBLIC,
            LocalDateTime.now().plusDays(1),
            10);
    url.setShortCode("limited123");
    url.setClickLimit(10);
    when(urlService.createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class)))
        .thenReturn(url);

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shortCode").value("limited123"))
        .andExpect(jsonPath("$.clickLimit").value(10))
        .andExpect(jsonPath("$.remainingClicks").value(10));
  }

  @Test
  @WithMockUser
  void validateUrl_ValidUrl_ReturnsValid() throws Exception {
    // Given
    Map<String, String> request = Map.of("url", "https://www.google.com");

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/urls/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true))
        .andExpect(jsonPath("$.errors").isEmpty());
  }

  @Test
  @WithMockUser
  void validateUrl_InvalidUrl_ReturnsErrors() throws Exception {
    // Given
    Map<String, String> request = Map.of("url", "javascript:alert('xss')");
    when(validationService.validateUrl(anyString())).thenReturn(invalidResult);

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/urls/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(false))
        .andExpect(jsonPath("$.errors").isNotEmpty());
  }

  @Test
  @WithUserDetails("test@example.com")
  void resetClickCount_ValidUrl_ResetsSuccessfully() throws Exception {
    // Given - create a URL first
    CreateUrlRequest createRequest =
        new CreateUrlRequest(
            "https://clicktest.com",
            "clicktest123",
            UrlVisibility.PUBLIC,
            LocalDateTime.now().plusDays(1),
            1);
    when(urlService.createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class)))
        .thenReturn(url);

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/v1/urls")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
                    .with(csrf()))
            .andExpect(status().isCreated())
            .andReturn();

    String urlId =
        objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    // When & Then
    url.setClickLimit(0);
    when(urlService.resetClickCount(any(UUID.class), any(UUID.class))).thenReturn(url);

    mockMvc
        .perform(post("/api/v1/urls/" + urlId + "/reset-clicks").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clickCount").value(0));
  }
}
