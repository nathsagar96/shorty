package com.shorty.users;

import com.shorty.urls.Url;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "users",
    indexes = {@Index(name = "idx_user_email", columnList = "email", unique = true)})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String firstName;

  private String lastName;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false)
  private boolean accountExpired = false;

  @Column(nullable = false)
  private boolean accountLocked = false;

  @Column(nullable = false)
  private boolean credentialsExpired = false;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Url> urls = new ArrayList<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;

  protected User() {}

  public User(String email, String firstName, String lastName, String passwordHash) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.passwordHash = passwordHash;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isAccountExpired() {
    return accountExpired;
  }

  public void setAccountExpired(boolean accountExpired) {
    this.accountExpired = accountExpired;
  }

  public boolean isAccountLocked() {
    return accountLocked;
  }

  public void setAccountLocked(boolean accountLocked) {
    this.accountLocked = accountLocked;
  }

  public boolean isCredentialsExpired() {
    return credentialsExpired;
  }

  public void setCredentialsExpired(boolean credentialsExpired) {
    this.credentialsExpired = credentialsExpired;
  }

  public List<Url> getUrls() {
    return urls;
  }

  public void setUrls(List<Url> urls) {
    this.urls = urls;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
