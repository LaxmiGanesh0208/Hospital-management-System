package com.pm.authservice.dto;

import com.pm.authservice.model.User;
import java.time.LocalDate;
import java.util.UUID;

public record UserProfileDTO(UUID id, String email, String role, String fullName,
    String mobile, LocalDate dateOfBirth, String gender, String address,
    String emergencyContact, UUID doctorId) {
  public static UserProfileDTO from(User user) {
    return new UserProfileDTO(user.getId(), user.getEmail(), user.getRole(),
        user.getFullName(), user.getMobile(), user.getDateOfBirth(), user.getGender(),
        user.getAddress(), user.getEmergencyContact(), user.getDoctorId());
  }
}
