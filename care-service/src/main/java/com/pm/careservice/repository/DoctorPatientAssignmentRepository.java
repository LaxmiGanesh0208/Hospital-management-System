package com.pm.careservice.repository;

import com.pm.careservice.model.DoctorPatientAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorPatientAssignmentRepository extends JpaRepository<DoctorPatientAssignment, UUID> {
  List<DoctorPatientAssignment> findByDoctorIdAndStatusOrderByCreatedAtDesc(UUID doctorId, String status);
  List<DoctorPatientAssignment> findByStatusOrderByCreatedAtAsc(String status);
}
