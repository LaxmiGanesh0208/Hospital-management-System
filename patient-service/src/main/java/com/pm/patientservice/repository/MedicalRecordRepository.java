package com.pm.patientservice.repository;

import com.pm.patientservice.model.MedicalRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
  List<MedicalRecord> findByPatientEmailIgnoreCaseOrderByRecordedAtDesc(String email);
}
