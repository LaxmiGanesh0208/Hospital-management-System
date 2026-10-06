package com.pm.careservice.config;

import com.pm.careservice.model.LabTest;
import com.pm.careservice.repository.LabTestRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Creates a fictional laboratory catalogue for the local demonstration only. */
@Component
public class DemoLabDataInitializer implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(DemoLabDataInitializer.class);
  private static final String DEMO_NOTICE = "DEMO ONLY — fictional catalogue entry for testing this application. Not a real laboratory service; do not use for patient care.";
  private static final List<DemoTest> DEMO_TESTS = List.of(
      new DemoTest("DEMO-CBC", "DEMO: Complete Blood Count (CBC)", "Hematology",
          "Sample blood-count catalogue entry. " + DEMO_NOTICE, "450.00", false),
      new DemoTest("DEMO-LIPID", "DEMO: Lipid Profile", "Biochemistry",
          "Sample lipid-panel catalogue entry. " + DEMO_NOTICE, "600.00", true),
      new DemoTest("DEMO-THYROID", "DEMO: Thyroid Profile (T3, T4, TSH)", "Endocrinology",
          "Sample thyroid-panel catalogue entry. " + DEMO_NOTICE, "750.00", false),
      new DemoTest("DEMO-URINE", "DEMO: Urine Routine Examination", "Pathology",
          "Sample urine-test catalogue entry. " + DEMO_NOTICE, "250.00", false),
      new DemoTest("DEMO-GLUCOSE", "DEMO: Fasting Blood Glucose", "Biochemistry",
          "Sample glucose-test catalogue entry. " + DEMO_NOTICE, "180.00", true));

  private final LabTestRepository tests;
  private final boolean enabled;

  public DemoLabDataInitializer(LabTestRepository tests,
      @Value("${DEMO_LAB_TESTS_ENABLED:false}") boolean enabled) {
    this.tests = tests;
    this.enabled = enabled;
  }

  @Override
  public void run(String... args) {
    if (!enabled) return;
    int added = 0;
    for (DemoTest sample : DEMO_TESTS) {
      if (tests.existsByCode(sample.code())) continue;
      LabTest test = new LabTest();
      test.setCode(sample.code());
      test.setName(sample.name());
      test.setCategory(sample.category());
      test.setDescription(sample.description());
      test.setPrice(new BigDecimal(sample.price()));
      test.setFastingRequired(sample.fastingRequired());
      test.setActive(true);
      tests.save(test);
      added++;
    }
    if (added > 0) log.info("Seeded {} fictional demonstration laboratory tests", added);
  }

  private record DemoTest(String code, String name, String category, String description,
      String price, boolean fastingRequired) {}
}
