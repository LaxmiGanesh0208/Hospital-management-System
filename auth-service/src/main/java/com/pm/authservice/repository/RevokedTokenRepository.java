package com.pm.authservice.repository;

import com.pm.authservice.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {}
