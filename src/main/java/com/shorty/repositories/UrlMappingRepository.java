package com.shorty.repositories;

import com.shorty.entities.UrlMapping;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UrlMapping u WHERE u.shortCode = :shortCode")
    Optional<UrlMapping> findByShortCodeForUpdate(@Param("shortCode") String shortCode);

    Optional<UrlMapping> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying
    @Query("DELETE FROM UrlMapping u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    int deleteExpiredMappings(@Param("now") Instant now);

    @Query("SELECT COUNT(u) FROM UrlMapping u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    long countExpiredMappings(@Param("now") Instant now);
}
