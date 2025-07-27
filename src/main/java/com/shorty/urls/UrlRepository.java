package com.shorty.urls;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Url, UUID> {

  Optional<Url> findByShortCodeAndActiveTrue(String shortCode);

  boolean existsByShortCode(String shortCode);

  List<Url> findByUserIdOrderByCreatedAtDesc(UUID userId);

  Page<Url> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

  @Query(
      "SELECT u FROM Url u WHERE u.visibility = 'PUBLIC' AND u.active = true ORDER BY u.createdAt DESC")
  Page<Url> findPublicUrls(Pageable pageable);

  @Query("SELECT COUNT(u) FROM Url u WHERE u.user.id = :userId AND u.active = true")
  long countActiveUrlsByUserId(@Param("userId") UUID userId);

  @Query(
      "SELECT u FROM Url u WHERE u.user.id = :userId AND u.expiresAt IS NOT NULL AND u.expiresAt <= :threshold AND u.active = true")
  List<Url> findUrlsExpiringBefore(
      @Param("userId") UUID userId, @Param("threshold") LocalDateTime threshold);

  @Query(
      "SELECT u FROM Url u WHERE u.expiresAt IS NOT NULL AND u.expiresAt <= :now AND u.active = true")
  List<Url> findExpiredUrls(@Param("now") LocalDateTime now);
}
