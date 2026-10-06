package com.pm.careservice.service;

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
import com.pm.careservice.repository.DoctorPatientAssignmentRepository;
import com.pm.careservice.repository.AppointmentRepository;
import com.pm.careservice.repository.AvailabilityRepository;
import com.pm.careservice.repository.DoctorRepository;
import com.pm.careservice.repository.LabBookingRepository;
import com.pm.careservice.repository.LabTestRepository;
import java.util.List;
import java.util.UUID;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CareService {
  private final DoctorRepository doctors;
  private final AvailabilityRepository availability;
  private final AppointmentRepository appointments;
  private final LabTestRepository tests;
  private final LabBookingRepository labBookings;
  private final BillingClient billingClient;
  private final DoctorPatientAssignmentRepository assignments;

  public CareService(DoctorRepository doctors, AvailabilityRepository availability,
      AppointmentRepository appointments, LabTestRepository tests,
      LabBookingRepository labBookings, BillingClient billingClient,
      DoctorPatientAssignmentRepository assignments) {
    this.doctors = doctors;
    this.availability = availability;
    this.appointments = appointments;
    this.tests = tests;
    this.labBookings = labBookings;
    this.billingClient = billingClient;
    this.assignments = assignments;
  }

  public List<Doctor> doctors() { return doctors.findByActiveTrueOrderByNameAsc(); }
  public Doctor doctor(UUID id) { return doctors.findById(id).filter(Doctor::isActive)
      .orElseThrow(() -> notFound("Doctor not found")); }
  public List<Doctor> searchDoctors(String query, String specialization) {
    String q = query == null ? "" : query.trim().toLowerCase();
    String spec = specialization == null ? "" : specialization.trim().toLowerCase();
    return doctors().stream().filter(item -> q.isBlank() || item.getName().toLowerCase().contains(q)
        || item.getSpecialization().toLowerCase().contains(q))
        .filter(item -> spec.isBlank() || item.getSpecialization().toLowerCase().equals(spec)).toList();
  }
  public Doctor createDoctor(DoctorRequest request) {
    Doctor doctor = new Doctor();
    doctor.setName(request.name()); doctor.setSpecialization(request.specialization());
    doctor.setQualification(request.qualification()); doctor.setYearsExperience(request.yearsExperience());
    doctor.setConsultationFee(request.consultationFee()); doctor.setAvatarUrl(request.avatarUrl());
    doctor.setBio(request.bio()); doctor.setActive(request.active() == null || request.active());
    return doctors.save(doctor);
  }
  public Doctor updateDoctor(UUID id, DoctorRequest request) {
    Doctor doctor = doctors.findById(id).orElseThrow(() -> notFound("Doctor not found"));
    doctor.setName(request.name()); doctor.setSpecialization(request.specialization());
    doctor.setQualification(request.qualification()); doctor.setYearsExperience(request.yearsExperience());
    doctor.setConsultationFee(request.consultationFee()); doctor.setAvatarUrl(request.avatarUrl());
    doctor.setBio(request.bio()); doctor.setActive(request.active() == null || request.active());
    return doctors.save(doctor);
  }
  public DoctorAvailability addAvailability(UUID doctorId, AvailabilityRequest request) {
    doctor(doctorId);
    if (!request.endTime().isAfter(request.startTime())) throw badRequest("End time must follow start time");
    DoctorAvailability slot = new DoctorAvailability(); slot.setDoctorId(doctorId);
    slot.setDate(request.date()); slot.setStartTime(request.startTime()); slot.setEndTime(request.endTime());
    return availability.save(slot);
  }
  public List<DoctorAvailability> availability(UUID doctorId, java.time.LocalDate date) {
    doctor(doctorId);
    java.time.LocalDate today = java.time.LocalDate.now();
    if (date.isBefore(today)) return List.of();
    List<DoctorAvailability> slots = availability.findByDoctorIdAndDateAndAvailableTrueOrderByStartTime(doctorId, date);
    return date.equals(today) ? slots.stream().filter(slot -> slot.getStartTime().isAfter(LocalTime.now())).toList() : slots;
  }
  public List<DoctorAvailability> upcomingAvailability(int days) {
    int range = Math.max(1, Math.min(days, 14));
    java.time.LocalDate today = java.time.LocalDate.now();
    List<DoctorAvailability> slots = availability.findByDateBetweenAndAvailableTrueOrderByDateAscStartTimeAsc(today, today.plusDays(range));
    LocalTime now = LocalTime.now();
    return slots.stream().filter(slot -> !slot.getDate().equals(today) || slot.getStartTime().isAfter(now)).toList();
  }
  @Transactional
  public Appointment book(String email, AppointmentRequest request) {
    DoctorAvailability slot = availability.findAvailableForUpdate(request.slotId())
        .orElseThrow(() -> badRequest("This appointment slot is no longer available"));
    if (slot.getDate().isBefore(java.time.LocalDate.now())
        || (slot.getDate().equals(java.time.LocalDate.now()) && !slot.getStartTime().isAfter(LocalTime.now()))) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This appointment time has passed. Please choose a future slot.");
    }
    Doctor doctor = doctor(slot.getDoctorId());
    slot.setAvailable(false); availability.save(slot);
    Appointment appointment = new Appointment(); appointment.setDoctorId(doctor.getId());
    appointment.setDoctorName(doctor.getName()); appointment.setSlotId(slot.getId());
    appointment.setPatientEmail(email); appointment.setDate(slot.getDate());
    appointment.setStartTime(slot.getStartTime()); appointment.setEndTime(slot.getEndTime());
    appointment.setNotes(request.notes());
    Appointment saved = appointments.save(appointment);
    billingClient.createInvoice(new com.pm.careservice.dto.InvoiceCommand(email, "APPOINTMENT",
        saved.getId().toString(), "Doctor consultation with " + doctor.getName(),
        doctor.getConsultationFee(), "INR"));
    return saved;
  }
  public List<Appointment> appointmentsFor(String email) { return appointments.findByPatientEmailOrderByDateDescStartTimeDesc(email); }
  public List<Appointment> allAppointments() { return appointments.findAll(); }
  public List<Appointment> appointmentsForDoctor(UUID doctorId) {
    if (doctorId == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor account is not linked to a profile");
    return appointments.findByDoctorIdOrderByDateDescStartTimeDesc(doctorId);
  }
  public DoctorPatientAssignment assignPatient(DoctorPatientAssignmentRequest request, String submitter) {
    Doctor doctor = doctor(request.doctorId());
    if (request.patientEmail() == null || request.patientEmail().isBlank()) throw badRequest("Patient email is required");
    DoctorPatientAssignment assignment = new DoctorPatientAssignment();
    assignment.setDoctorId(doctor.getId()); assignment.setDoctorName(doctor.getName());
    assignment.setPatientEmail(request.patientEmail().trim().toLowerCase(java.util.Locale.ROOT));
    assignment.setNote(request.note()); assignment.setSubmittedBy(submitter);
    return assignments.save(assignment);
  }
  public List<DoctorPatientAssignment> doctorPatients(UUID doctorId) {
    if (doctorId == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor account is not linked to a profile");
    return assignments.findByDoctorIdAndStatusOrderByCreatedAtDesc(doctorId, "APPROVED");
  }
  public List<DoctorPatientAssignment> doctorPatientReviewQueue() { return assignments.findByStatusOrderByCreatedAtAsc("PENDING_REVIEW"); }
  @Transactional
  public DoctorPatientAssignment reviewDoctorPatient(UUID id, String reviewer, String decision, String note) {
    DoctorPatientAssignment item = assignments.findById(id).orElseThrow(() -> notFound("Assignment not found"));
    if (!"PENDING_REVIEW".equals(item.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment is not awaiting review");
    if (reviewer.equalsIgnoreCase(item.getSubmittedBy())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A different staff member must review this assignment");
    if (!List.of("APPROVED", "REJECTED").contains(decision)) throw badRequest("Choose approve or reject");
    item.setStatus(decision); item.setReviewedBy(reviewer); item.setReviewNote(note);
    return assignments.save(item);
  }
  @Transactional
  public Appointment cancelAppointment(String email, UUID id, boolean admin) {
    Appointment item = appointments.findById(id).orElseThrow(() -> notFound("Appointment not found"));
    if (!admin && !item.getPatientEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    if (!"CONFIRMED".equals(item.getStatus())) throw badRequest("Only confirmed appointments can be cancelled");
    item.setStatus("CANCELLED");
    availability.findById(item.getSlotId()).ifPresent(slot -> { slot.setAvailable(true); availability.save(slot); });
    return appointments.save(item);
  }
  public List<LabTest> tests() { return tests.findApprovedForCatalogue(); }
  public List<LabTest> labTestReviewQueue() { return tests.findByApprovalStatusOrderByNameAsc("PENDING_REVIEW"); }
  @Transactional
  public LabTest reviewLabTest(UUID id, String reviewer, String decision, String note) {
    LabTest test = tests.findById(id).orElseThrow(() -> notFound("Laboratory test not found"));
    if (!"PENDING_REVIEW".equals(test.getApprovalStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This test is not awaiting review");
    if (reviewer.equalsIgnoreCase(test.getSubmittedBy())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A different staff member must review this submission");
    if (!List.of("APPROVED", "REJECTED").contains(decision)) throw badRequest("Choose approve or reject");
    test.setApprovalStatus(decision); test.setReviewedBy(reviewer); test.setReviewNote(note);
    return tests.save(test);
  }
  public LabTest test(UUID id) { return tests.findById(id).filter(LabTest::isActive).filter(t -> t.getApprovalStatus() == null || "APPROVED".equals(t.getApprovalStatus()))
      .orElseThrow(() -> notFound("Laboratory test not found")); }
  public List<LocalTime> labAvailability(UUID testId, java.time.LocalDate date) {
    test(testId);
    if (date.isBefore(java.time.LocalDate.now())) throw badRequest("Date must not be in the past");
    List<LocalTime> available = new java.util.ArrayList<>();
    for (LocalTime time = LocalTime.of(8, 0); time.isBefore(LocalTime.of(17, 0)); time = time.plus(30, ChronoUnit.MINUTES)) {
      if (date.equals(java.time.LocalDate.now()) && !time.isAfter(LocalTime.now())) continue;
      if (!labBookings.existsByTestIdAndDateAndTime(testId, date, time)) available.add(time);
    }
    return available;
  }
  public LabTest createTest(LabTestRequest request, String submitter) {
    if (tests.existsByCode(request.code())) throw new ResponseStatusException(HttpStatus.CONFLICT, "A laboratory test with this code already exists");
    LabTest test = new LabTest(); test.setCode(request.code()); test.setName(request.name());
    test.setCategory(request.category()); test.setDescription(request.description());
    test.setPrice(request.price()); test.setFastingRequired(request.fastingRequired());
    test.setActive(request.active() == null || request.active()); test.setApprovalStatus("PENDING_REVIEW");
    test.setSubmittedBy(submitter); return tests.save(test);
  }
  public LabTest updateTest(UUID id, LabTestRequest request, String submitter) {
    LabTest test = tests.findById(id).orElseThrow(() -> notFound("Laboratory test not found"));
    if (tests.existsByCode(request.code()) && !test.getCode().equalsIgnoreCase(request.code()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "A laboratory test with this code already exists");
    test.setCode(request.code()); test.setName(request.name()); test.setCategory(request.category());
    test.setDescription(request.description()); test.setPrice(request.price());
    test.setFastingRequired(request.fastingRequired()); test.setActive(request.active() == null || request.active());
    test.setApprovalStatus("PENDING_REVIEW"); test.setSubmittedBy(submitter);
    return tests.save(test);
  }
  public LabBooking bookLabTest(String email, LabBookingRequest request) {
    if (!List.of("IN_PERSON", "HOME_COLLECTION").contains(request.collectionMethod()))
      throw badRequest("Collection method must be IN_PERSON or HOME_COLLECTION");
    LabTest test = test(request.testId());
    if (!labAvailability(test.getId(), request.date()).contains(request.time()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This laboratory slot is unavailable");
    LabBooking booking = new LabBooking(); booking.setTestId(test.getId());
    booking.setTestName(test.getName()); booking.setPatientEmail(email);
    booking.setDate(request.date()); booking.setTime(request.time());
    booking.setCollectionMethod(request.collectionMethod()); booking.setPrice(test.getPrice());
    LabBooking saved = labBookings.save(booking);
    billingClient.createInvoice(new com.pm.careservice.dto.InvoiceCommand(email, "LAB_TEST",
        saved.getId().toString(), "Laboratory test: " + test.getName(), test.getPrice(), "INR"));
    return saved;
  }
  public List<LabBooking> labBookingsFor(String email) {
    return labBookings.findByPatientEmailOrderByDateDescTimeDesc(email).stream().map(item -> {
      if (!"COMPLETED".equals(item.getStatus())) item.setResultSummary(null);
      return item;
    }).toList();
  }
  public List<LabBooking> allLabBookings() { return labBookings.findAll(); }
  public List<LabBooking> labResultReviewQueue() { return labBookings.findByStatusOrderByDateAscTimeAsc("RESULT_PENDING_REVIEW"); }
  public java.util.Map<String, Long> adminSummary() {
    java.time.LocalDate today = java.time.LocalDate.now();
    return java.util.Map.of("activeDoctors", doctors.countByActiveTrue(),
        "appointmentsToday", appointments.countByDateAndStatus(today, "CONFIRMED"),
        "labBookingsToday", labBookings.countByDateAndStatus(today, "BOOKED"));
  }
  public LabBooking labBooking(String email, UUID id, boolean admin) {
    LabBooking item = labBookings.findById(id).orElseThrow(() -> notFound("Laboratory booking not found"));
    if (!admin && !item.getPatientEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return item;
  }
  public LabBooking publishResult(UUID id, String submitter, LabResultRequest request) {
    LabBooking item = labBookings.findById(id).orElseThrow(() -> notFound("Laboratory booking not found"));
    if (!List.of("BOOKED", "RESULT_REJECTED").contains(item.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking cannot accept a result now");
    item.setStatus("RESULT_PENDING_REVIEW"); item.setResultSummary(request.resultSummary());
    item.setResultSubmittedBy(submitter); return labBookings.save(item);
  }
  @Transactional
  public LabBooking reviewLabResult(UUID id, String reviewer, String decision, String note) {
    LabBooking item = labBookings.findById(id).orElseThrow(() -> notFound("Laboratory booking not found"));
    if (!"RESULT_PENDING_REVIEW".equals(item.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This result is not awaiting review");
    if (reviewer.equalsIgnoreCase(item.getResultSubmittedBy())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A different staff member must review this result");
    if (!List.of("APPROVED", "REJECTED").contains(decision)) throw badRequest("Choose approve or reject");
    item.setStatus("APPROVED".equals(decision) ? "COMPLETED" : "RESULT_REJECTED");
    item.setResultReviewedBy(reviewer); item.setResultReviewNote(note);
    return labBookings.save(item);
  }
  private static ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
  private static ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
}
