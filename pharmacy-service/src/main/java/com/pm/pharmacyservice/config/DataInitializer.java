package com.pm.pharmacyservice.config;

import com.pm.pharmacyservice.model.Medication;
import com.pm.pharmacyservice.repository.MedicationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initPharmacyData(MedicationRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(seed("Amoxicillin 500mg", "MED-001", 150, 20, "15.50", "Antibiotics", true));
                repository.save(seed("Lisinopril 10mg", "MED-002", 80, 15, "22.00", "Cardiovascular", true));
                repository.save(seed("Metformin 850mg", "MED-003", 200, 30, "18.75", "Diabetes care", true));
                repository.save(seed("Ibuprofen 400mg", "MED-004", 50, 10, "8.99", "Pain relief", false));
            }
        };
    }

    private Medication seed(String name, String code, int stock, int reorder, String price, String category, boolean prescription) {
        Medication item = new Medication(null, name, code, stock, reorder, new BigDecimal(price));
        item.setCategory(category);
        item.setRequiresPrescription(prescription);
        return item;
    }
}
