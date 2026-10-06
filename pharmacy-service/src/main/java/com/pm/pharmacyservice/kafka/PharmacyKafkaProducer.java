package com.pm.pharmacyservice.kafka;

import com.pm.pharmacyservice.model.Prescription;
import com.pm.pharmacyservice.model.Medication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pharmacy.events.PrescriptionEvent;
import pharmacy.events.MedicationInventoryEvent;

@Service
public class PharmacyKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(PharmacyKafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public PharmacyKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPrescriptionEvent(Prescription prescription) {
        PrescriptionEvent event = PrescriptionEvent.newBuilder()
                .setPrescriptionId(prescription.getId().toString())
                .setPatientId(prescription.getPatientId().toString())
                .setMedicationId(prescription.getMedicationId().toString())
                .setMedicationName(prescription.getMedicationName())
                .setQuantity(prescription.getQuantity())
                .setStatus(prescription.getStatus())
                .setTimestamp(System.currentTimeMillis())
                .build();

        try {
            kafkaTemplate.send("pharmacy-events", prescription.getPatientId().toString(), event.toByteArray());
            log.info("Published PrescriptionEvent to Kafka topic pharmacy-events: [PrescriptionId={}, PatientId={}, Status={}]",
                    prescription.getId(), prescription.getPatientId(), prescription.getStatus());
        } catch (Exception e) {
            log.error("Failed to send PrescriptionEvent to Kafka: {}", e.getMessage());
        }
    }

    public void sendInventoryEvent(Medication medication, int previousStock, String changeType) {
        MedicationInventoryEvent event = MedicationInventoryEvent.newBuilder()
                .setMedicationId(medication.getId().toString())
                .setMedicationName(medication.getName())
                .setPreviousStock(previousStock)
                .setCurrentStock(medication.getStockQuantity())
                .setReorderThreshold(medication.getReorderThreshold())
                .setChangeType(changeType)
                .setTimestamp(System.currentTimeMillis())
                .build();
        try {
            kafkaTemplate.send("pharmacy-inventory-events", medication.getId().toString(), event.toByteArray());
            log.info("Published pharmacy inventory event: medicationId={}, stock={}, threshold={}, type={}",
                    medication.getId(), medication.getStockQuantity(), medication.getReorderThreshold(), changeType);
        } catch (Exception e) {
            log.error("Failed to send pharmacy inventory event for {}: {}", medication.getId(), e.getMessage());
        }
    }
}
