package com.pm.patientservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Patient {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotNull
  private String name;

  @NotNull
  @Email
  @Column(unique = true)
  private String email;

  @NotNull
  private String address;

  private String mobile;

  private String gender;

  private String emergencyContact;

  @NotNull
  private LocalDate dateOfBirth;

  @NotNull
  private LocalDate registeredDate;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public @NotNull String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  public @NotNull @Email String getEmail() {
    return email;
  }

  public void setEmail(@NotNull @Email String email) {
    this.email = email;
  }

  public @NotNull String getAddress() {
    return address;
  }

  public void setAddress(@NotNull String address) {
    this.address = address;
  }

  public String getMobile() { return mobile; }
  public void setMobile(String mobile) { this.mobile = mobile; }
  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }
  public String getEmergencyContact() { return emergencyContact; }
  public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

  public @NotNull LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(@NotNull LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public @NotNull LocalDate getRegisteredDate() {
    return registeredDate;
  }

  public void setRegisteredDate(@NotNull LocalDate registeredDate) {
    this.registeredDate = registeredDate;
  }

}
