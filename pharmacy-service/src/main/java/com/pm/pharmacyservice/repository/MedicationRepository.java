package com.pm.pharmacyservice.repository;

import com.pm.pharmacyservice.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    Optional<Medication> findByName(String name);
    boolean existsByName(String name);
    boolean existsByCode(String code);
    java.util.List<Medication> findByApprovalStatusOrderByNameAsc(String approvalStatus);
    java.util.List<Medication> findByApprovalStatusInOrderByNameAsc(java.util.Collection<String> approvalStatuses);
    @Query("select m from Medication m where coalesce(m.approvalStatus, 'APPROVED') = 'APPROVED' order by m.name")
    java.util.List<Medication> findApprovedForCatalogue();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medication m where m.id = :id")
    Optional<Medication> findForUpdate(@Param("id") UUID id);
}
