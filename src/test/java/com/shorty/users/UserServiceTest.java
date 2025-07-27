package com.shorty.users;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.shorty.common.exception.ValidationException;
import com.shorty.users.dto.UserRegistrationRequest;
import com.shorty.users.dto.UserResponse;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private UserService userService;

  @Test
  void registerUser_ValidRequest_ReturnsUserResponse() {
    // Given
    UserRegistrationRequest request =
        new UserRegistrationRequest("test@example.com", "John", "Doe", "password123");

    User savedUser =
        User.builder()
            .id(UUID.randomUUID())
            .email(request.email())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .password(request.password())
            .urls(new ArrayList<>())
            .build();

    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    // When
    UserResponse result = userService.registerUser(request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.email()).isEqualTo(request.email());
    assertThat(result.firstName()).isEqualTo(request.firstName());
    assertThat(result.lastName()).isEqualTo(request.lastName());

    verify(userRepository).existsByEmail(request.email());
    verify(passwordEncoder).encode(request.password());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void registerUser_EmailExists_ThrowsValidationException() {
    // Given
    UserRegistrationRequest request =
        new UserRegistrationRequest("test@example.com", "John", "Doe", "password123");

    when(userRepository.existsByEmail(anyString())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.registerUser(request))
        .isInstanceOf(ValidationException.class)
        .hasMessage("User already exists with email: " + request.email());

    verify(userRepository).existsByEmail(anyString());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void findByEmail_ExistingUser_ReturnsUser() {
    // Given
    String email = "test@example.com";
    User user = new User(email, "John", "Doe", "hashedPassword");

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

    // When
    Optional<User> result = userService.findByEmail(email);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo(email);

    verify(userRepository).findByEmail(anyString());
  }
}
