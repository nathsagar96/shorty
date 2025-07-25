package com.shorty.security;

import com.shorty.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
    log.info("UserRepository injected into CustomUserDetailsService successfully.");
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByEmail(username)
        .map(CustomUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}
