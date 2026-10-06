package com.pm.careservice.repository;

import com.pm.careservice.model.Doctor;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
  List<Doctor> findByActiveTrueOrderByNameAsc();
  boolean existsByName(String name);
  long countByActiveTrue();
}
