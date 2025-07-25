package com.shorty.users;

import com.shorty.security.CustomUserDetails;
import com.shorty.urls.Url;
import com.shorty.urls.UrlService;
import com.shorty.urls.dto.UrlResponse;
import com.shorty.users.dto.ChangePasswordRequest;
import com.shorty.users.dto.UpdateProfileRequest;
import com.shorty.users.dto.UserResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;
  private final UrlService urlService;
  private final String baseUrl;

  public UserController(
      UserService userService,
      UrlService urlService,
      @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
    this.userService = userService;
    this.urlService = urlService;
    this.baseUrl = baseUrl;
  }

  @GetMapping("/profile")
  public ResponseEntity<UserResponse> getCurrentUserProfile(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UserResponse user = userService.getUserProfile(userDetails.user().getId());
    return ResponseEntity.ok(user);
  }

  @GetMapping("/urls")
  public ResponseEntity<List<UrlResponse>> getCurrentUserUrls(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<Url> urls = urlService.getUserUrls(userDetails.user().getId());
    List<UrlResponse> response = urls.stream().map(url -> UrlResponse.from(url, baseUrl)).toList();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/urls/paginated")
  public ResponseEntity<Page<UrlResponse>> getCurrentUserUrlsPaginated(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Url> urls = urlService.getUserUrls(userDetails.user().getId(), pageable);
    Page<UrlResponse> response = urls.map(url -> UrlResponse.from(url, baseUrl));

    return ResponseEntity.ok(response);
  }

  @PutMapping("/profile")
  public ResponseEntity<UserResponse> updateProfile(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody UpdateProfileRequest request) {
    UserResponse user =
        userService.updateUserProfile(
            userDetails.user().getId(), request.firstName(), request.lastName());
    return ResponseEntity.ok(user);
  }

  @PostMapping("/change-password")
  public ResponseEntity<Void> changePassword(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody ChangePasswordRequest request) {
    userService.changePassword(
        userDetails.user().getId(), request.currentPassword(), request.newPassword());
    return ResponseEntity.ok().build();
  }
}
