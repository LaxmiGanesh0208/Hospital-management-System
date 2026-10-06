package com.pm.authservice.repository;

import com.pm.authservice.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
  Optional<User> findByEmailIgnoreCase(String email);
  Optional<User> findByMobile(String mobile);
  boolean existsByMobile(String mobile);
  boolean existsByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
  boolean existsByMobileAndIdNot(String mobile, UUID id);
  java.util.List<User> findByRoleInOrderByFullNameAsc(java.util.Collection<String> roles);
}
