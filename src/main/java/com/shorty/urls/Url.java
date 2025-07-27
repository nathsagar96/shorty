package com.shorty.urls;

import com.shorty.users.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UrlVisibility visibility = UrlVisibility.PUBLIC;

  private LocalDateTime expiresAt;

  @Builder.Default
  @Column(nullable = false)
  private int clickLimit = -1;

  @Builder.Default
  @Column(nullable = false)
  private int clickCount = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;

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
    if (clickLimit < 0) return -1;
    return Math.max(0, clickLimit - clickCount);
  }
}
