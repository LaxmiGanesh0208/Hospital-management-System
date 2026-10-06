package com.pm.careservice.repository;

import com.pm.careservice.model.Appointment;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
  List<Appointment> findByPatientEmailOrderByDateDescStartTimeDesc(String patientEmail);
  List<Appointment> findByDoctorIdOrderByDateDescStartTimeDesc(UUID doctorId);
  long countByDateAndStatus(LocalDate date, String status);
}
