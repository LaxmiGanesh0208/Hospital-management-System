package com.pm.authservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity @Table(name = "revoked_tokens")
public class RevokedToken {
  @Id @Column(length = 64) private String tokenHash;
  @Column(nullable = false) private Instant revokedAt;
  protected RevokedToken() {}
  public RevokedToken(String tokenHash) { this.tokenHash = tokenHash; }
  @PrePersist void onCreate() { if (revokedAt == null) revokedAt = Instant.now(); }
  public String getTokenHash() { return tokenHash; }
}
