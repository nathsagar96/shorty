package com.shorty.users;

import com.shorty.urls.Url;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    name = "users",
    indexes = {@Index(name = "idx_user_email", columnList = "email", unique = true)})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false, length = 128)
  private String email;

  @Column(nullable = false, length = 64)
  private String firstName;

  @Column(length = 64)
  private String lastName;

  @Column(nullable = false, length = 128)
  private String password;

  @Builder.Default
  @Column(nullable = false)
  private boolean enabled = true;

  @Builder.Default
  @Column(nullable = false)
  private boolean accountExpired = false;

  @Builder.Default
  @Column(nullable = false)
  private boolean accountLocked = false;

  @Builder.Default
  @Column(nullable = false)
  private boolean credentialsExpired = false;

  @Builder.Default
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Url> urls = new ArrayList<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;

  public User(String email, String firstName, String lastName, String password) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
