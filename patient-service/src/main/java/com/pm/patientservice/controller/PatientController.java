package com.pm.patientservice.controller;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.dto.MedicalRecordRequest;
import com.pm.patientservice.dto.NotificationRequest;
import com.pm.patientservice.model.MedicalRecord;
import com.pm.patientservice.model.PatientNotification;
import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import com.pm.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing Patients")
public class PatientController {

  private final PatientService patientService;

  public PatientController(PatientService patientService) {
    this.patientService = patientService;
  }

  @GetMapping
  @Operation(summary = "Get Patients")
  public ResponseEntity<List<PatientResponseDTO>> getPatients(
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAdmin(role);
    List<PatientResponseDTO> patients = patientService.getPatients();
    return ResponseEntity.ok().body(patients);
  }

  @GetMapping("/me")
  public ResponseEntity<PatientResponseDTO> getMyPatientProfile(
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email) {
    requireEmail(email);
    return patientService.getPatientByEmail(email).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/me/medical-history")
  public List<MedicalRecord> medicalHistory(
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email) {
    return patientService.medicalHistory(requireEmail(email));
  }

  @GetMapping("/medical-history")
  public List<MedicalRecord> medicalHistoryForAdmin(
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestParam String email) {
    requireAdmin(role); return patientService.medicalHistory(email);
  }

  @PostMapping("/medical-history")
  @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public MedicalRecord addMedicalRecord(
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @Valid @RequestBody MedicalRecordRequest request) {
    requireAdmin(role); return patientService.addMedicalRecord(request);
  }

  @GetMapping("/me/notifications")
  public List<PatientNotification> myNotifications(
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email) {
    return patientService.notifications(requireEmail(email));
  }

  @PatchMapping("/me/notifications/{id}/read")
  public PatientNotification markNotificationRead(
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @PathVariable UUID id) {
    return patientService.markNotificationRead(requireEmail(email), id);
  }

  @PostMapping("/notifications")
  @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public PatientNotification addNotification(
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @Valid @RequestBody NotificationRequest request) {
    requireAdmin(role); return patientService.addNotification(request);
  }

  @PostMapping
  @Operation(summary = "Create a new Patient")
  public ResponseEntity<PatientResponseDTO> createPatient(
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @Validated({Default.class, CreatePatientValidationGroup.class})
      @RequestBody PatientRequestDTO patientRequestDTO) {

    requireEmail(email);
    if (!isAdmin(role) && !email.equalsIgnoreCase(patientRequestDTO.getEmail())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Patients can only create their own profile");
    }

    PatientResponseDTO patientResponseDTO = patientService.createPatient(
        patientRequestDTO);

    return ResponseEntity.ok().body(patientResponseDTO);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a new Patient")
  public ResponseEntity<PatientResponseDTO> updatePatient(
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @PathVariable UUID id,
      @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {

    requireEmail(email);
    if (!isAdmin(role) && !patientService.patientBelongsToEmail(id, email)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (!isAdmin(role) && !email.equalsIgnoreCase(patientRequestDTO.getEmail())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email changes must be made through the account profile");
    }

    PatientResponseDTO patientResponseDTO = patientService.updatePatient(id,
        patientRequestDTO);

    return ResponseEntity.ok().body(patientResponseDTO);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a Patient")
  public ResponseEntity<Void> deletePatient(
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @PathVariable UUID id) {
    requireAdmin(role);
    patientService.deletePatient(id);
    return ResponseEntity.noContent().build();
  }

  private static String requireEmail(String email) {
    if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return email;
  }
  private static boolean isAdmin(String role) { return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role); }
  private static void requireAdmin(String role) {
    if (!isAdmin(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator role required");
  }
}
