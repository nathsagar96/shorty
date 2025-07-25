package com.shorty.urls;

import com.shorty.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "urls",
    indexes = {
      @Index(name = "idx_url_short_code", columnList = "shortCode", unique = true),
      @Index(name = "idx_url_user_id", columnList = "user_id"),
      @Index(name = "idx_url_created_at", columnList = "createdAt")
    })
public class Url {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Size(max = 2048)
  @Column(nullable = false, length = 2048)
  private String originalUrl;

  @Column(nullable = false, unique = true)
  private String shortCode;

  @Column(nullable = false)
  private boolean active = true;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UrlVisibility visibility = UrlVisibility.PUBLIC;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;

  protected Url() {}

  public Url(String originalUrl, String shortCode, User user) {
    this.originalUrl = originalUrl;
    this.shortCode = shortCode;
    this.user = user;
  }

  public Url(String originalUrl, String shortCode) {
    this.originalUrl = originalUrl;
    this.shortCode = shortCode;
  }

  public UUID getId() {
    return id;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public void setOriginalUrl(String originalUrl) {
    this.originalUrl = originalUrl;
  }

  public String getShortCode() {
    return shortCode;
  }

  public void setShortCode(String shortCode) {
    this.shortCode = shortCode;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public UrlVisibility getVisibility() {
    return visibility;
  }

  public void setVisibility(UrlVisibility visibility) {
    this.visibility = visibility;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public boolean isOwnedBy(User user) {
    return this.user != null && this.user.getId().equals(user.getId());
  }
}
