package com.shorty.security;

import com.shorty.users.dto.AuthenticationResponse;
import com.shorty.users.dto.UserLoginRequest;
import com.shorty.users.dto.UserRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationService authenticationService;

  public AuthController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @Valid @RequestBody UserRegistrationRequest request) {
    return ResponseEntity.ok(authenticationService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @Valid @RequestBody UserLoginRequest request) {
    return ResponseEntity.ok(authenticationService.authenticate(request));
  }
}
