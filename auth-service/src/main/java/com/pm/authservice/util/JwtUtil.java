package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import com.pm.authservice.dto.AuthIdentityDTO;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final Key secretKey;

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    byte[] keyBytes = Base64.getDecoder()
        .decode(secret.getBytes(StandardCharsets.UTF_8));
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(String email, String role) {
    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *10)) // 10 hours
        .signWith(secretKey)
        .compact();
  }

  public String generateToken(UUID userId, String email, String role) {
    return generateToken(userId, email, role, null);
  }

  public String generateToken(UUID userId, String email, String role, UUID doctorId) {
    var builder = Jwts.builder().subject(email).claim("role", role).claim("userId", userId.toString());
    if (doctorId != null) builder.claim("doctorId", doctorId.toString());
    return builder
        .id(UUID.randomUUID().toString()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
        .signWith(secretKey).compact();
  }

  public AuthIdentityDTO inspectToken(String token) {
    var claims = Jwts.parser().verifyWith((SecretKey) secretKey).build()
        .parseSignedClaims(token).getPayload();
    String userIdClaim = claims.get("userId", String.class);
    UUID userId = userIdClaim == null ? null : UUID.fromString(userIdClaim);
    String doctorIdClaim = claims.get("doctorId", String.class);
    UUID doctorId = doctorIdClaim == null ? null : UUID.fromString(doctorIdClaim);
    return new AuthIdentityDTO(userId, claims.getSubject(), claims.get("role", String.class), doctorId);
  }

  public void validateToken(String token) {
    try {
      Jwts.parser().verifyWith((SecretKey) secretKey)
          .build()
          .parseSignedClaims(token);
    } catch (SignatureException e) {
      throw new JwtException("Invalid JWT signature");
    } catch (JwtException e) {
      throw new JwtException("Invalid JWT");
    }
  }
}
