package com.pm.careservice.repository;

import com.pm.careservice.model.LabBooking;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabBookingRepository extends JpaRepository<LabBooking, UUID> {
  List<LabBooking> findByPatientEmailOrderByDateDescTimeDesc(String patientEmail);
  boolean existsByTestIdAndDateAndTime(UUID testId, LocalDate date, LocalTime time);
  long countByDateAndStatus(LocalDate date, String status);
  List<LabBooking> findByStatusOrderByDateAscTimeAsc(String status);
}
