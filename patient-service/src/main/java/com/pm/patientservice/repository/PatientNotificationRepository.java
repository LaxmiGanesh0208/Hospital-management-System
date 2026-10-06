package com.pm.patientservice.repository;

import com.pm.patientservice.model.PatientNotification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientNotificationRepository extends JpaRepository<PatientNotification, UUID> {
  List<PatientNotification> findByPatientEmailIgnoreCaseOrderByCreatedAtDesc(String email);
  Optional<PatientNotification> findByIdAndPatientEmailIgnoreCase(UUID id, String email);
}
