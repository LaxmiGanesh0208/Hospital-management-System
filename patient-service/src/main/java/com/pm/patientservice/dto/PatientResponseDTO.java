package com.pm.patientservice.dto;

public class PatientResponseDTO {
  private String id;
  private String name;
  private String email;
  private String address;
  private String dateOfBirth;
  private String mobile;
  private String gender;
  private String emergencyContact;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getMobile() { return mobile; }
  public void setMobile(String mobile) { this.mobile = mobile; }
  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }
  public String getEmergencyContact() { return emergencyContact; }
  public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }


}
