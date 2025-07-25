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

  @Query(
      "SELECT u FROM Url u WHERE u.user.id = :userId AND u.clickLimit > 0 AND u.clickCount >= u.clickLimit")
  List<Url> findUrlsWithReachedClickLimit(@Param("userId") UUID userId);

  @Query("SELECT u FROM Url u WHERE u.passwordProtected = true AND u.user.id = :userId")
  List<Url> findPasswordProtectedUrls(@Param("userId") UUID userId);

  @Query(
      "SELECT u FROM Url u WHERE u.visibility = 'PUBLIC' AND u.active = true AND (u.expiresAt IS NULL OR u.expiresAt > :now) ORDER BY u.createdAt DESC")
  Page<Url> findPublicActiveUrls(@Param("now") LocalDateTime now, Pageable pageable);

  @Query(
      "SELECT u FROM Url u WHERE u.user.id = :userId AND u.active = true AND (u.expiresAt IS NULL OR u.expiresAt > :now)")
  Page<Url> findUserActiveUrls(
      @Param("userId") UUID userId, @Param("now") LocalDateTime now, Pageable pageable);

  @Query(
      "SELECT COUNT(u) FROM Url u WHERE u.user.id = :userId AND u.active = true AND (u.expiresAt IS NULL OR u.expiresAt > :now)")
  long countUserActiveUrls(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

  @Query("SELECT u FROM Url u WHERE u.createdAt >= :since AND u.active = true")
  List<Url> findActiveUrlsCreatedSince(@Param("since") LocalDateTime since);

  @Query(
      "SELECT u FROM Url u WHERE u.description LIKE %:keyword% OR u.originalUrl LIKE %:keyword% AND u.user.id = :userId")
  List<Url> searchUserUrls(@Param("userId") UUID userId, @Param("keyword") String keyword);

  @Query("SELECT u FROM Url u WHERE u.shortCode IN :shortCodes AND u.user.id = :userId")
  List<Url> findByShortCodesAndUserId(
      @Param("shortCodes") List<String> shortCodes, @Param("userId") UUID userId);

  void deleteByUserIdAndId(UUID userId, UUID urlId);
}
