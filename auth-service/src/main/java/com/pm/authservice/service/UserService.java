package com.pm.authservice.service;

import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepository;
import java.util.Optional;
import java.util.Locale;
import java.util.UUID;
import com.pm.authservice.dto.ProfileUpdateRequestDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.dto.StaffAccountRequestDTO;
import com.pm.authservice.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private static final java.util.Set<String> STAFF_ROLES = java.util.Set.of(
      "PHARMACIST", "PHARMACY_REVIEWER", "LAB_TECH", "LAB_REVIEWER",
      "DOCTOR", "DOCTOR_REVIEWER");

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }
  
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmailIgnoreCase(normalizeEmail(email));
  }

  public Optional<User> findByMobile(String mobile) { return userRepository.findByMobile(normalizeMobile(mobile)); }

  @Transactional
  public User createStaffAccount(StaffAccountRequestDTO request) {
    String role = request.role().trim().toUpperCase(Locale.ROOT);
    if (!STAFF_ROLES.contains(role)) throw new IllegalArgumentException("Choose a supported department role");
    String email = normalizeEmail(request.email());
    if (userRepository.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("An account with this email already exists");
    if ("DOCTOR".equals(role) && request.doctorId() == null)
      throw new IllegalArgumentException("Choose the doctor profile linked to this account");
    if (!role.startsWith("DOCTOR") && request.doctorId() != null)
      throw new IllegalArgumentException("Only doctor accounts can be linked to a doctor profile");
    User user = new User(); user.setEmail(email); user.setPassword(passwordEncoder.encode(request.password()));
    user.setFullName(request.fullName().trim()); user.setRole(role); user.setDoctorId(request.doctorId());
    return userRepository.save(user);
  }

  public java.util.List<User> staffAccounts() {
    return userRepository.findByRoleInOrderByFullNameAsc(STAFF_ROLES);
  }

  @Transactional
  public User register(RegisterRequestDTO request) {
    String email = normalizeEmail(request.email());
    String mobile = normalizeMobile(request.mobile());
    if (userRepository.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("An account with this email already exists");
    if (userRepository.existsByMobile(mobile)) throw new IllegalArgumentException("An account with this mobile number already exists");
    User user = new User();
    user.setEmail(email); user.setMobile(mobile); user.setPassword(passwordEncoder.encode(request.password()));
    user.setRole("PATIENT"); user.setFullName(request.fullName().trim());
    user.setDateOfBirth(request.dateOfBirth()); user.setGender(request.gender().trim());
    user.setAddress(request.address().trim()); user.setEmergencyContact(normalizeMobile(request.emergencyContact()));
    return userRepository.save(user);
  }

  @Transactional
  public User updateProfile(String email, ProfileUpdateRequestDTO request) {
    User user = findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    String nextMobile = normalizeMobile(request.mobile());
    if (userRepository.existsByMobileAndIdNot(nextMobile, user.getId())) throw new IllegalArgumentException("An account with this mobile number already exists");
    user.setMobile(nextMobile); user.setFullName(request.fullName().trim());
    user.setAddress(request.address().trim()); user.setEmergencyContact(normalizeMobile(request.emergencyContact()));
    return userRepository.save(user);
  }

  private static String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
  private static String normalizeMobile(String mobile) { return mobile.replaceAll("[\\s()-]", ""); }
}
