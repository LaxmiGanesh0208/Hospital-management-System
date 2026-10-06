package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.dto.StaffAccountRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pm.authservice.model.RevokedToken;
import com.pm.authservice.repository.RevokedTokenRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final RevokedTokenRepository revokedTokens;

  public AuthService(UserService userService, PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil, RevokedTokenRepository revokedTokens) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.revokedTokens = revokedTokens;
  }

  public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
    return authenticateUser(loginRequestDTO).map(u -> jwtUtil.generateToken(u.getId(), u.getEmail(), u.getRole(), u.getDoctorId()));
  }

  public Optional<User> authenticateUser(LoginRequestDTO request) {
    String identifier = request.getLoginIdentifier();
    if (identifier == null || identifier.isBlank() || request.getPassword() == null) return Optional.empty();
    Optional<User> account = identifier.contains("@") ? userService.findByEmail(identifier) : userService.findByMobile(identifier);
    return account.filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()));
  }

  public User register(RegisterRequestDTO request) { return userService.register(request); }
  public User createStaffAccount(StaffAccountRequestDTO request) { return userService.createStaffAccount(request); }
  public java.util.List<User> staffAccounts() { return userService.staffAccounts(); }
  public String tokenFor(User user) { return jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getDoctorId()); }
  public User updateProfile(String email, com.pm.authservice.dto.ProfileUpdateRequestDTO request) { return userService.updateProfile(email, request); }
  public Optional<User> getUser(String email) { return userService.findByEmail(email); }
  public com.pm.authservice.dto.AuthIdentityDTO inspect(String token) {
    if (!validateToken(token)) throw new JwtException("Invalid or revoked JWT");
    var identity = jwtUtil.inspectToken(token);
    if (identity.userId() != null) return identity;
    return userService.findByEmail(identity.email())
        .map(user -> new com.pm.authservice.dto.AuthIdentityDTO(user.getId(), user.getEmail(), user.getRole(), user.getDoctorId()))
        .orElseThrow(() -> new JwtException("Unknown account"));
  }

  @Transactional
  public void revoke(String token) {
    jwtUtil.validateToken(token);
    String tokenHash = hashToken(token);
    if (!revokedTokens.existsById(tokenHash)) revokedTokens.save(new RevokedToken(tokenHash));
  }

  private static String hashToken(String token) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
          .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 is unavailable", ex); }
  }

  public boolean validateToken(String token) {
    try {
      jwtUtil.validateToken(token);
      return !revokedTokens.existsById(hashToken(token));
    } catch (JwtException e){
      return false;
    }
  }
}
