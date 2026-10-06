package com.pm.authservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import java.time.LocalDate;
import jakarta.persistence.Column;

@Entity
@Table(name="users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String role;
  private String fullName;
  @Column(unique = true)
  private String mobile;
  private LocalDate dateOfBirth;
  private String gender;
  private String address;
  private String emergencyContact;
  @Column(name = "doctor_id")
  private UUID doctorId;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getMobile() { return mobile; }
  public void setMobile(String mobile) { this.mobile = mobile; }
  public LocalDate getDateOfBirth() { return dateOfBirth; }
  public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getEmergencyContact() { return emergencyContact; }
  public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
  public UUID getDoctorId() { return doctorId; }
  public void setDoctorId(UUID doctorId) { this.doctorId = doctorId; }
}
