package com.pm.authservice.dto;

public class LoginResponseDTO {
  private final String token;
  private final java.util.UUID userId;
  private final String email;
  private final String role;
  private final String fullName;
  private final String mobile;
  private final java.util.UUID doctorId;

  public LoginResponseDTO(String token) {
    this.token = token;
    this.userId = null;
    this.email = null;
    this.role = null;
    this.fullName = null;
    this.mobile = null;
    this.doctorId = null;
  }

  public LoginResponseDTO(String token, com.pm.authservice.model.User user) {
    this.token = token;
    this.userId = user.getId();
    this.email = user.getEmail();
    this.role = user.getRole();
    this.fullName = user.getFullName();
    this.mobile = user.getMobile();
    this.doctorId = user.getDoctorId();
  }

  public String getToken() {
    return token;
  }
  public java.util.UUID getUserId() { return userId; }
  public String getEmail() { return email; }
  public String getRole() { return role; }
  public String getFullName() { return fullName; }
  public String getMobile() { return mobile; }
  public java.util.UUID getDoctorId() { return doctorId; }
}
