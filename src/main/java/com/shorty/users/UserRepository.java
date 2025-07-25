package com.shorty.users;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
  long countEnabledUsers();

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.urls WHERE u.id = :id")
  Optional<User> findByIdWithUrls(UUID id);
}
