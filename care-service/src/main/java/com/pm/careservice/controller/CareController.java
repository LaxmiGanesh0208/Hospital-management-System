package com.pm.careservice.controller;

import com.pm.careservice.dto.AppointmentRequest;
import com.pm.careservice.dto.AvailabilityRequest;
import com.pm.careservice.dto.DoctorRequest;
import com.pm.careservice.dto.LabBookingRequest;
import com.pm.careservice.dto.LabResultRequest;
import com.pm.careservice.dto.LabTestRequest;
import com.pm.careservice.dto.DoctorPatientAssignmentRequest;
import com.pm.careservice.model.Appointment;
import com.pm.careservice.model.Doctor;
import com.pm.careservice.model.DoctorAvailability;
import com.pm.careservice.model.LabBooking;
import com.pm.careservice.model.LabTest;
import com.pm.careservice.model.DoctorPatientAssignment;
import com.pm.careservice.service.CareService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/care")
public class CareController {
  private final CareService service;
  public CareController(CareService service) { this.service = service; }

  @GetMapping("/doctors")
  public List<Doctor> doctors(@RequestParam(required = false) String q,
      @RequestParam(required = false) String specialization) { return service.searchDoctors(q, specialization); }
  @GetMapping("/doctors/availability/upcoming")
  public List<DoctorAvailability> upcomingDoctorAvailability(@RequestParam(defaultValue = "7") int days) {
    return service.upcomingAvailability(days);
  }
  @GetMapping("/doctors/{id}")
  public Doctor doctor(@PathVariable UUID id) { return service.doctor(id); }
  @PostMapping("/doctors")
  @ResponseStatus(HttpStatus.CREATED)
  public Doctor createDoctor(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @Valid @RequestBody DoctorRequest request) { requireAdmin(role); return service.createDoctor(request); }
  @PutMapping("/doctors/{id}")
  public Doctor updateDoctor(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @PathVariable UUID id, @Valid @RequestBody DoctorRequest request) { requireAdmin(role); return service.updateDoctor(id, request); }
  @PostMapping("/doctors/{id}/availability")
  @ResponseStatus(HttpStatus.CREATED)
  public DoctorAvailability addAvailability(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @PathVariable UUID id, @Valid @RequestBody AvailabilityRequest request) { requireAdmin(role); return service.addAvailability(id, request); }
  @GetMapping("/doctors/{id}/availability")
  public List<DoctorAvailability> availability(@PathVariable UUID id, @RequestParam LocalDate date) { return service.availability(id, date); }

  @PostMapping("/appointments")
  @ResponseStatus(HttpStatus.CREATED)
  public Appointment book(@RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @Valid @RequestBody AppointmentRequest request) { return service.book(requireEmail(email), request); }
  @GetMapping("/appointments/my")
  public List<Appointment> myAppointments(@RequestHeader(value = "X-Authenticated-Email", required = false) String email) {
    return service.appointmentsFor(requireEmail(email));
  }
  @GetMapping("/appointments")
  public List<Appointment> allAppointments(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAdmin(role); return service.allAppointments();
  }
  @GetMapping("/appointments/doctor")
  public List<Appointment> doctorAppointments(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Doctor-Id", required = false) UUID doctorId) {
    requireAnyRole(role, "DOCTOR"); return service.appointmentsForDoctor(doctorId);
  }
  @PostMapping("/doctor-patients")
  @ResponseStatus(HttpStatus.CREATED)
  public DoctorPatientAssignment assignPatient(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @Valid @RequestBody DoctorPatientAssignmentRequest request) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN"); return service.assignPatient(request, requireEmail(email));
  }
  @GetMapping("/doctor-patients")
  public List<DoctorPatientAssignment> doctorPatients(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Doctor-Id", required = false) UUID doctorId) {
    requireAnyRole(role, "DOCTOR"); return service.doctorPatients(doctorId);
  }
  @GetMapping("/doctor-patients/review-queue")
  public List<DoctorPatientAssignment> doctorPatientReviewQueue(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "DOCTOR_REVIEWER"); return service.doctorPatientReviewQueue();
  }
  @PatchMapping("/doctor-patients/{id}/review")
  public DoctorPatientAssignment reviewDoctorPatient(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @PathVariable UUID id, @RequestBody ReviewRequest request) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "DOCTOR_REVIEWER");
    return service.reviewDoctorPatient(id, requireEmail(email), request.decision(), request.note());
  }
  @PatchMapping("/appointments/{id}/cancel")
  public Appointment cancel(@RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role, @PathVariable UUID id) {
    return service.cancelAppointment(requireEmail(email), id, isAdmin(role));
  }

  @GetMapping("/lab-tests")
  public List<LabTest> labTests() { return service.tests(); }
  @GetMapping("/lab-tests/{id}")
  public LabTest labTest(@PathVariable UUID id) { return service.test(id); }
  @GetMapping("/lab-tests/review-queue")
  public List<LabTest> labTestReviewQueue(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "LAB_REVIEWER"); return service.labTestReviewQueue();
  }
  @GetMapping("/lab-tests/{id}/availability")
  public List<java.time.LocalTime> labAvailability(@PathVariable UUID id, @RequestParam LocalDate date) {
    return service.labAvailability(id, date);
  }
  @PostMapping("/lab-tests")
  @ResponseStatus(HttpStatus.CREATED)
  public LabTest createTest(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @Valid @RequestBody LabTestRequest request) { requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "LAB_TECH"); return service.createTest(request, requireEmail(email)); }
  @PatchMapping("/lab-tests/{id}/review")
  public LabTest reviewLabTest(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @PathVariable UUID id, @RequestBody ReviewRequest request) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "LAB_REVIEWER");
    return service.reviewLabTest(id, requireEmail(email), request.decision(), request.note());
  }
  @PutMapping("/lab-tests/{id}")
  public LabTest updateTest(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @PathVariable UUID id, @Valid @RequestBody LabTestRequest request) { requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "LAB_TECH"); return service.updateTest(id, request, requireEmail(email)); }
  @PostMapping("/lab-bookings")
  @ResponseStatus(HttpStatus.CREATED)
  public LabBooking bookLab(@RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @Valid @RequestBody LabBookingRequest request) { return service.bookLabTest(requireEmail(email), request); }
  @GetMapping("/lab-bookings/my")
  public List<LabBooking> myLabBookings(@RequestHeader(value = "X-Authenticated-Email", required = false) String email) {
    return service.labBookingsFor(requireEmail(email));
  }
  @GetMapping("/lab-bookings")
  public List<LabBooking> allLabBookings(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "LAB_TECH", "LAB_REVIEWER"); return service.allLabBookings();
  }
  @GetMapping("/lab-bookings/review-queue")
  public List<LabBooking> labResultReviewQueue(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "LAB_REVIEWER"); return service.labResultReviewQueue();
  }
  @PatchMapping("/lab-bookings/{id}/review")
  public LabBooking reviewLabResult(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @PathVariable UUID id, @RequestBody ReviewRequest request) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "LAB_REVIEWER");
    return service.reviewLabResult(id, requireEmail(email), request.decision(), request.note());
  }
  @GetMapping("/lab-bookings/{id}")
  public LabBooking labBooking(@RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role, @PathVariable UUID id) {
    return service.labBooking(requireEmail(email), id, isAdmin(role));
  }
  @PatchMapping("/lab-bookings/{id}/result")
  public LabBooking publishResult(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @PathVariable UUID id, @Valid @RequestBody LabResultRequest request) { requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "LAB_TECH"); return service.publishResult(id, requireEmail(email), request); }

  @GetMapping("/admin/summary")
  public java.util.Map<String, Long> adminSummary(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAdmin(role); return service.adminSummary();
  }

  private static String requireEmail(String email) {
    if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return email;
  }
  private static boolean isAdmin(String role) { return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role); }
  private static void requireAdmin(String role) {
    if (!isAdmin(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator role required");
  }
  private static void requireAnyRole(String role, String... allowed) {
    for (String item : allowed) if (item.equals(role)) return;
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient department role");
  }
  public record ReviewRequest(String decision, String note) {}
}
