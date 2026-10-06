package com.pm.careservice.repository;

import com.pm.careservice.model.DoctorAvailability;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilityRepository extends JpaRepository<DoctorAvailability, UUID> {
  List<DoctorAvailability> findByDoctorIdAndDateAndAvailableTrueOrderByStartTime(UUID doctorId, LocalDate date);
  List<DoctorAvailability> findByDateBetweenAndAvailableTrueOrderByDateAscStartTimeAsc(LocalDate from, LocalDate to);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from DoctorAvailability a where a.id = :id and a.available = true")
  Optional<DoctorAvailability> findAvailableForUpdate(@Param("id") UUID id);
}
