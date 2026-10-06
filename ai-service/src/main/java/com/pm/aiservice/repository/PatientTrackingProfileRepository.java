package com.pm.aiservice.repository;

import com.pm.aiservice.model.PatientTrackingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientTrackingProfileRepository extends JpaRepository<PatientTrackingProfile, String> {
    List<PatientTrackingProfile> findByRiskStatus(String riskStatus);
}
