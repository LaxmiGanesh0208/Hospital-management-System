package com.pm.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDTO {
  private String identifier;
  private String email;
  private String mobile;
  @NotBlank @Size(min = 8, max = 72) private String password;
  public String getIdentifier() { return identifier; }
  public void setIdentifier(String identifier) { this.identifier = identifier; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getMobile() { return mobile; }
  public void setMobile(String mobile) { this.mobile = mobile; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getLoginIdentifier() {
    if (identifier != null && !identifier.isBlank()) return identifier.trim();
    if (mobile != null && !mobile.isBlank()) return mobile.trim();
    return email == null ? null : email.trim();
  }
}
