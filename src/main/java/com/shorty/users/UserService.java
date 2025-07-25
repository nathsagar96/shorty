package com.shorty.users;

import com.shorty.common.exception.ResourceNotFoundException;
import com.shorty.common.exception.ValidationException;
import com.shorty.users.dto.UserRegistrationRequest;
import com.shorty.users.dto.UserResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UserResponse registerUser(UserRegistrationRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new ValidationException("User already exists with email: " + request.email());
    }

    User user =
        new User(
            request.email(),
            request.firstName(),
            request.lastName(),
            passwordEncoder.encode(request.password()));

    User savedUser = userRepository.save(user);
    return UserResponse.from(savedUser);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Transactional(readOnly = true)
  public Optional<User> findById(UUID id) {
    return userRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public UserResponse getUserProfile(UUID userId) {
    User user =
        userRepository
            .findByIdWithUrls(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    return UserResponse.from(user);
  }

  public UserResponse updateUserProfile(UUID userId, String firstName, String lastName) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    user.setFirstName(firstName);
    user.setLastName(lastName);

    User savedUser = userRepository.save(user);
    return UserResponse.from(savedUser);
  }

  public void changePassword(UUID userId, String currentPassword, String newPassword) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new ValidationException("Current password is incorrect");
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream().map(UserResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public long getUserCount() {
    return userRepository.count();
  }
}
