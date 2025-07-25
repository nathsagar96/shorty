package com.shorty.urls;

import com.shorty.users.User;
import jakarta.persistence.*;
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
      @Index(name = "idx_url_created_at", columnList = "createdAt"),
      @Index(name = "idx_url_expires_at", columnList = "expiresAt"),
      @Index(name = "idx_url_active_expires", columnList = "active,expiresAt")
    })
public class Url {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 2048)
  private String originalUrl;

  @Column(nullable = false, unique = true, length = 50)
  private String shortCode;

  @Column(nullable = false)
  private boolean active = true;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UrlVisibility visibility = UrlVisibility.PUBLIC;

  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private int clickLimit = -1; // -1 means unlimited

  @Column(nullable = false)
  private int clickCount = 0;

  @Column(length = 500)
  private String description;

  @Column(nullable = false)
  private boolean passwordProtected = false;

  @Column private String passwordHash;

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

  public boolean isExpired() {
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }

  public boolean isClickLimitReached() {
    return clickLimit > 0 && clickCount >= clickLimit;
  }

  public boolean isAccessible() {
    return active && !isExpired() && !isClickLimitReached();
  }

  public void incrementClickCount() {
    this.clickCount++;
  }

  public boolean isOwnedBy(User user) {
    return this.user == null || !this.user.getId().equals(user.getId());
  }

  public int getRemainingClicks() {
    if (clickLimit < 0) return -1; // Unlimited
    return Math.max(0, clickLimit - clickCount);
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

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public int getClickLimit() {
    return clickLimit;
  }

  public void setClickLimit(int clickLimit) {
    this.clickLimit = clickLimit;
  }

  public int getClickCount() {
    return clickCount;
  }

  public void setClickCount(int clickCount) {
    this.clickCount = clickCount;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isPasswordProtected() {
    return passwordProtected;
  }

  public void setPasswordProtected(boolean passwordProtected) {
    this.passwordProtected = passwordProtected;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
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
}
