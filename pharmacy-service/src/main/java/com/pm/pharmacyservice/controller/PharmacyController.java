package com.pm.pharmacyservice.controller;

import com.pm.pharmacyservice.dto.PharmacyOrderRequest;
import com.pm.pharmacyservice.dto.MedicationRequestDTO;
import com.pm.pharmacyservice.dto.PrescriptionRequestDTO;
import com.pm.pharmacyservice.model.Medication;
import com.pm.pharmacyservice.model.PharmacyOrder;
import com.pm.pharmacyservice.model.Prescription;
import com.pm.pharmacyservice.service.PharmacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pharmacy")
@Tag(name = "Pharmacy", description = "Medication inventory, prescriptions, and pharmacy orders")
public class PharmacyController {
  private final PharmacyService pharmacyService;
  public PharmacyController(PharmacyService pharmacyService) { this.pharmacyService = pharmacyService; }

  @GetMapping("/medications")
  @Operation(summary = "Get the medication catalogue")
  public List<Medication> getMedications(
      @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
      @org.springframework.web.bind.annotation.RequestParam(required = false) String category,
      @org.springframework.web.bind.annotation.RequestParam(defaultValue = "false") boolean availableOnly) {
    return pharmacyService.searchMedications(q, category, availableOnly);
  }

  @PostMapping("/medications")
  @ResponseStatus(HttpStatus.CREATED)
  public Medication addMedication(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @Valid @RequestBody MedicationRequestDTO medication) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "PHARMACIST");
    return pharmacyService.addMedication(medication, email == null ? "unknown" : email);
  }

  @GetMapping("/medications/review-queue")
  public List<Medication> medicationReviewQueue(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "PHARMACY_REVIEWER");
    return pharmacyService.reviewQueue();
  }

  @PatchMapping("/medications/{id}/review")
  public Medication reviewMedication(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @PathVariable UUID id, @RequestBody ReviewRequest request) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "PHARMACY_REVIEWER");
    return pharmacyService.reviewMedication(id, email == null ? "unknown" : email, request.decision(), request.note());
  }

  @PostMapping("/prescriptions")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Issue a prescription as a doctor or administrator")
  public Prescription createPrescription(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @Valid @RequestBody PrescriptionRequestDTO request) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "DOCTOR");
    return pharmacyService.createPrescription(request);
  }

  @PutMapping("/prescriptions/{id}/dispense")
  public Prescription dispensePrescription(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @PathVariable UUID id) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "PHARMACIST");
    return pharmacyService.dispensePrescription(id);
  }

  @GetMapping("/prescriptions/patient/{patientId}")
  public List<Prescription> getPatientPrescriptions(
      @RequestHeader(value = "X-Authenticated-User-Id", required = false) String userId,
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @PathVariable UUID patientId) {
    if (!isAdmin(role) && !patientId.toString().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return pharmacyService.getPrescriptionsByPatient(patientId);
  }

  @PostMapping("/orders")
  @ResponseStatus(HttpStatus.CREATED)
  public PharmacyOrder placeOrder(@RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @RequestHeader(value = "X-Authenticated-User-Id", required = false) String userId,
      @Valid @RequestBody PharmacyOrderRequest request) {
    if (email == null || email.isBlank() || userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return pharmacyService.placeOrder(email, UUID.fromString(userId), request);
  }

  @GetMapping("/orders/my")
  public List<PharmacyOrder> myOrders(@RequestHeader(value = "X-Authenticated-Email", required = false) String email) {
    if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return pharmacyService.getOrdersForPatient(email);
  }

  @GetMapping("/orders")
  public List<PharmacyOrder> allOrders(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "PHARMACIST");
    return pharmacyService.getOrders();
  }

  @PatchMapping("/orders/{id}/status")
  public PharmacyOrder updateOrderStatus(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @PathVariable UUID id, @RequestBody OrderStatusRequest request) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "PHARMACIST");
    return pharmacyService.updateOrderStatus(id, request.status());
  }

  public record OrderStatusRequest(String status) {}
  public record ReviewRequest(String decision, String note) {}
  private static boolean isAdmin(String role) { return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role); }
  private static void requireAnyRole(String role, String... allowed) {
    for (String item : allowed) if (item.equals(role)) return;
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role for this pharmacy operation");
  }
}
