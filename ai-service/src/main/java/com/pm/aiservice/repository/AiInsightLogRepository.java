package com.pm.aiservice.repository;

import com.pm.aiservice.model.AiInsightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiInsightLogRepository extends JpaRepository<AiInsightLog, UUID> {
    List<AiInsightLog> findByPatientId(String patientId);
    List<AiInsightLog> findByDepartment(String department);
    List<AiInsightLog> findBySeverity(String severity);
}
