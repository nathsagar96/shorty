package com.shorty.urls;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shorty.SpringSecurityConfiguration;
import com.shorty.urls.dto.CreateUrlRequest;
import com.shorty.urls.dto.UpdateUrlRequest;
import com.shorty.users.User;
import java.util.List;
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

@Import(SpringSecurityConfiguration.class)
@AutoConfigureMockMvc
@WebMvcTest(controllers = UrlController.class)
class UrlControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UrlService urlService;

  private Url url;
  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "Test", "User", "hashedPassword");
    url = new Url("http://example.com", "example", user);

    try {
      var idField = url.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(url, UUID.randomUUID());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithUserDetails("test@example.com")
  void createShortUrl_validRequest_createsUrl() throws Exception {
    // given
    CreateUrlRequest validRequest =
        new CreateUrlRequest("http://example.com", "example", UrlVisibility.PUBLIC);
    when(urlService.createShortUrl(
            any(String.class), any(String.class), any(UrlVisibility.class), any(User.class)))
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
            any(String.class), any(String.class), any(UrlVisibility.class), any(User.class));
  }

  @Test
  @WithUserDetails("test@example.com")
  void createShortUrl_invalidRequest_returnsBadRequest() throws Exception {
    // given
    CreateUrlRequest invalidRequest = new CreateUrlRequest("", "example", UrlVisibility.PUBLIC);

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
            any(String.class), any(String.class), any(UrlVisibility.class), any(User.class));
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
        new UpdateUrlRequest("http://updated-example.com", UrlVisibility.PRIVATE);
    Url updatedUrl = new Url("http://updated-example.com", "example", user);
    when(urlService.updateUrl(
            any(UUID.class), any(UUID.class), any(String.class), any(UrlVisibility.class)))
        .thenReturn(updatedUrl);

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
        .andExpect(jsonPath("$.originalUrl").value("http://updated-example.com"));

    // verify interaction
    verify(urlService)
        .updateUrl(any(UUID.class), any(UUID.class), any(String.class), any(UrlVisibility.class));
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
    given(urlService.getUrlCount()).willReturn(1L);

    // when & then
    mockMvc
        .perform(get("/api/v1/urls/count").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").value(1L));

    // verify interaction
    verify(urlService).getUrlCount();
  }
}
