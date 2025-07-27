package com.shorty.security;

import com.shorty.users.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record CustomUserDetails(User user) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return !user.isAccountExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return !user.isAccountLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return !user.isCredentialsExpired();
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }
}
