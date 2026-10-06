package com.pm.aiservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.aiservice.service.AiEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;
import pharmacy.events.PrescriptionEvent;
import pharmacy.events.MedicationInventoryEvent;

@Service
public class AiKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AiKafkaConsumer.class);
    private final AiEngineService aiEngineService;

    public AiKafkaConsumer(AiEngineService aiEngineService) {
        this.aiEngineService = aiEngineService;
    }

    @KafkaListener(topics = "patient", groupId = "ai-service-group")
    public void consumePatientEvent(byte[] eventBytes) {
        try {
            PatientEvent event = PatientEvent.parseFrom(eventBytes);
            log.info("[AI SERVICE KAFKA] Received Patient Event: [PatientId={}, Name={}]", event.getPatientId(), event.getName());
            aiEngineService.processPatientEvent(event);
        } catch (InvalidProtocolBufferException e) {
            log.error("[AI SERVICE KAFKA] Deserialization error on Patient event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "pharmacy-events", groupId = "ai-service-group")
    public void consumePharmacyEvent(byte[] eventBytes) {
        try {
            PrescriptionEvent event = PrescriptionEvent.parseFrom(eventBytes);
            log.info("[AI SERVICE KAFKA] Received Pharmacy Event: [PrescriptionId={}, Medication={}]", event.getPrescriptionId(), event.getMedicationName());
            aiEngineService.processPrescriptionEvent(event);
        } catch (InvalidProtocolBufferException e) {
            log.error("[AI SERVICE KAFKA] Deserialization error on Pharmacy event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "pharmacy-inventory-events", groupId = "ai-service-group")
    public void consumePharmacyInventoryEvent(byte[] eventBytes) {
        try {
            MedicationInventoryEvent event = MedicationInventoryEvent.parseFrom(eventBytes);
            aiEngineService.processPharmacyInventoryEvent(event);
        } catch (InvalidProtocolBufferException e) {
            log.error("[AI SERVICE KAFKA] Invalid pharmacy inventory event: {}", e.getMessage());
        }
    }
}
