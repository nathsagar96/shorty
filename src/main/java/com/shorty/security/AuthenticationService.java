package com.shorty.security;

import com.shorty.common.exception.ValidationException;
import com.shorty.users.User;
import com.shorty.users.UserService;
import com.shorty.users.dto.AuthenticationResponse;
import com.shorty.users.dto.UserLoginRequest;
import com.shorty.users.dto.UserRegistrationRequest;
import com.shorty.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserService userService;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(UserRegistrationRequest request) {
    UserResponse user = userService.registerUser(request);

    User userEntity = userService.findById(user.id()).orElseThrow();
    CustomUserDetails userDetails = new CustomUserDetails(userEntity);

    String jwtToken = jwtService.generateToken(userDetails);

    return AuthenticationResponse.create(jwtToken, jwtService.getExpirationTime());
  }

  public AuthenticationResponse authenticate(UserLoginRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    } catch (AuthenticationException e) {
      throw new ValidationException("Invalid email or password");
    }

    User user =
        userService
            .findByEmail(request.email())
            .orElseThrow(() -> new ValidationException("User not found"));

    CustomUserDetails userDetails = new CustomUserDetails(user);
    String jwtToken = jwtService.generateToken(userDetails);

    return AuthenticationResponse.create(jwtToken, jwtService.getExpirationTime());
  }
}
