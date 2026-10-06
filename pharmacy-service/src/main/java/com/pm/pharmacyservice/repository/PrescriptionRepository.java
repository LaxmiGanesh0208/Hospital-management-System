package com.pm.pharmacyservice.repository;

import com.pm.pharmacyservice.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    List<Prescription> findByPatientId(UUID patientId);
    List<Prescription> findByStatus(String status);
    Optional<Prescription> findByIdAndPatientId(UUID id, UUID patientId);
}
