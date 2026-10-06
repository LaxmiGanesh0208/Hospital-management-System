package com.pm.pharmacyservice.repository;

import com.pm.pharmacyservice.model.PharmacyOrder;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, UUID> {
  List<PharmacyOrder> findByPatientEmailOrderByCreatedAtDesc(String patientEmail);
}
