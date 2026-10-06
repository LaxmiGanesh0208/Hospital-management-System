package com.pm.careservice.config;

import com.pm.careservice.model.Doctor;
import com.pm.careservice.model.DoctorAvailability;
import com.pm.careservice.repository.AvailabilityRepository;
import com.pm.careservice.repository.DoctorRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Creates fictional doctor profiles and schedules for the local demonstration only. */
@Component
public class DemoDoctorDataInitializer implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(DemoDoctorDataInitializer.class);
  private static final String DEMO_BIO = "FICTIONAL DEMO PROFILE — sample information for demonstrating appointment booking. Not a real clinician.";
  private static final List<DemoDoctor> DEMO_DOCTORS = List.of(
      new DemoDoctor("Demo Doctor - Cardiology", "Cardiology", "Demo profile", 8, "650.00"),
      new DemoDoctor("Demo Doctor - Dermatology", "Dermatology", "Demo profile", 6, "500.00"),
      new DemoDoctor("Demo Doctor - Pediatrics", "Pediatrics", "Demo profile", 10, "550.00"),
      new DemoDoctor("Demo Doctor - Neurology", "Neurology", "Demo profile", 12, "750.00"),
      new DemoDoctor("Demo Doctor - Orthopedics", "Orthopedics", "Demo profile", 9, "600.00"));
  private static final List<LocalTime> SAMPLE_START_TIMES = List.of(
      LocalTime.of(9, 0), LocalTime.of(10, 30), LocalTime.of(14, 0));

  private final DoctorRepository doctors;
  private final AvailabilityRepository availability;
  private final boolean enabled;

  public DemoDoctorDataInitializer(DoctorRepository doctors, AvailabilityRepository availability,
      @Value("${DEMO_DOCTORS_ENABLED:false}") boolean enabled) {
    this.doctors = doctors;
    this.availability = availability;
    this.enabled = enabled;
  }

  @Override
  public void run(String... args) {
    if (!enabled) return;
    LocalDate firstDay = LocalDate.now().plusDays(1);
    int added = 0;
    for (DemoDoctor sample : DEMO_DOCTORS) {
      if (doctors.existsByName(sample.name())) continue;
      Doctor doctor = new Doctor();
      doctor.setName(sample.name());
      doctor.setSpecialization(sample.specialization());
      doctor.setQualification(sample.qualification());
      doctor.setYearsExperience(sample.yearsExperience());
      doctor.setConsultationFee(new BigDecimal(sample.fee()));
      doctor.setBio(DEMO_BIO);
      doctor.setActive(true);
      Doctor saved = doctors.save(doctor);

      for (int dayOffset = 0; dayOffset < 2; dayOffset++) {
        LocalDate date = firstDay.plusDays(dayOffset);
        for (LocalTime start : SAMPLE_START_TIMES) {
          DoctorAvailability slot = new DoctorAvailability();
          slot.setDoctorId(saved.getId());
          slot.setDate(date);
          slot.setStartTime(start);
          slot.setEndTime(start.plusMinutes(30));
          slot.setAvailable(true);
          availability.save(slot);
        }
      }
      added++;
    }
    if (added > 0) log.info("Seeded {} fictional demonstration doctor profiles with sample schedules", added);
  }

  private record DemoDoctor(String name, String specialization, String qualification,
      int yearsExperience, String fee) {}
}
