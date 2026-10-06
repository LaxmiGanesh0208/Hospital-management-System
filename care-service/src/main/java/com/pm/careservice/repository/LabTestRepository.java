package com.pm.careservice.repository;

import com.pm.careservice.model.LabTest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabTestRepository extends JpaRepository<LabTest, UUID> {
  List<LabTest> findByActiveTrueAndApprovalStatusOrderByNameAsc(String approvalStatus);
  List<LabTest> findByApprovalStatusOrderByNameAsc(String approvalStatus);
  @org.springframework.data.jpa.repository.Query("select t from LabTest t where t.active = true and coalesce(t.approvalStatus, 'APPROVED') = 'APPROVED' order by t.name")
  List<LabTest> findApprovedForCatalogue();
  boolean existsByCode(String code);
}
