package com.pm.authservice.controller;

import com.pm.authservice.dto.AuthIdentityDTO;
import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.dto.ProfileUpdateRequestDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.dto.UserProfileDTO;
import com.pm.authservice.dto.StaffAccountRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.service.AuthService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService authService) { this.authService = authService; }

  @Operation(summary = "Create a patient account and return an access token")
  @PostMapping("/register")
  public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
    User user = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDTO(authService.tokenFor(user), user));
  }

  @Operation(summary = "Generate an access token using email or mobile and password")
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
    Optional<User> user = authService.authenticateUser(request);
    if (user.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    return ResponseEntity.ok(new LoginResponseDTO(authService.tokenFor(user.get()), user.get()));
  }

  @Operation(summary = "Validate a token")
  @GetMapping("/validate")
  public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
    String token = bearer(authHeader);
    return authService.validateToken(token) ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
    try { authService.revoke(bearer(authHeader)); return ResponseEntity.noContent().build(); }
    catch (JwtException | IllegalArgumentException exception) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
  }

  @Operation(summary = "Return verified identity claims for the API Gateway")
  @GetMapping("/introspect")
  public AuthIdentityDTO introspect(@RequestHeader("Authorization") String authHeader) {
    try { return authService.inspect(bearer(authHeader)); }
    catch (JwtException | IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); }
  }

  @GetMapping("/me")
  public UserProfileDTO me(@RequestHeader("Authorization") String authHeader) {
    AuthIdentityDTO identity = identity(authHeader);
    return authService.getUser(identity.email()).map(UserProfileDTO::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @PutMapping("/me")
  public UserProfileDTO updateMe(@RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody ProfileUpdateRequestDTO request) {
    AuthIdentityDTO identity = identity(authHeader);
    return UserProfileDTO.from(authService.updateProfile(identity.email(), request));
  }

  @PostMapping("/admin/staff")
  public ResponseEntity<UserProfileDTO> createStaff(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @Valid @RequestBody StaffAccountRequestDTO request) {
    requireAdministrator(role);
    return ResponseEntity.status(HttpStatus.CREATED).body(UserProfileDTO.from(authService.createStaffAccount(request)));
  }

  @GetMapping("/admin/staff")
  public java.util.List<UserProfileDTO> staffAccounts(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAdministrator(role);
    return authService.staffAccounts().stream().map(UserProfileDTO::from).toList();
  }

  private static void requireAdministrator(String role) {
    if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  private AuthIdentityDTO identity(String authorization) {
    try { return authService.inspect(bearer(authorization)); }
    catch (JwtException | IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); }
  }
  private static String bearer(String value) {
    if (value == null || !value.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return value.substring(7);
  }
}
