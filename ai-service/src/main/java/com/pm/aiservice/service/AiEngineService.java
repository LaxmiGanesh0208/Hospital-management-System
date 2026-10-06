package com.pm.aiservice.service;

import com.pm.aiservice.model.AiInsightLog;
import com.pm.aiservice.model.PatientTrackingProfile;
import com.pm.aiservice.repository.AiInsightLogRepository;
import com.pm.aiservice.repository.PatientTrackingProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patient.events.PatientEvent;
import pharmacy.events.PrescriptionEvent;
import pharmacy.events.MedicationInventoryEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AiEngineService {

    private static final Logger log = LoggerFactory.getLogger(AiEngineService.class);

    private final AiInsightLogRepository insightLogRepository;
    private final PatientTrackingProfileRepository profileRepository;

    public AiEngineService(AiInsightLogRepository insightLogRepository,
                            PatientTrackingProfileRepository profileRepository) {
        this.insightLogRepository = insightLogRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public void processPatientEvent(PatientEvent event) {
        log.info("[AI ENGINE] Processing Patient Department event for PatientId: {}", event.getPatientId());

        Optional<PatientTrackingProfile> profileOpt = profileRepository.findById(event.getPatientId());
        PatientTrackingProfile profile;
        if (profileOpt.isPresent()) {
            profile = profileOpt.get();
            profile.setPatientName(event.getName());
            profile.setPatientEmail(event.getEmail());
            profile.setLastActivity(LocalDateTime.now());
        } else {
            profile = new PatientTrackingProfile(event.getPatientId(), event.getName(), event.getEmail());
        }
        profileRepository.save(profile);

        AiInsightLog insight = new AiInsightLog(
                null,
                event.getPatientId(),
                "PATIENT_DEPARTMENT",
                "PATIENT_REGISTRATION",
                "AI tracked new patient registration: " + event.getName() + " (" + event.getEmail() + "). Initializing risk baseline.",
                "LOW",
                LocalDateTime.now()
        );
        insightLogRepository.save(insight);
    }

    @Transactional
    public void processPrescriptionEvent(PrescriptionEvent event) {
        log.info("[AI ENGINE] Processing Pharmacy Department event for PrescriptionId: {}, PatientId: {}",
                event.getPrescriptionId(), event.getPatientId());

        Optional<PatientTrackingProfile> profileOpt = profileRepository.findById(event.getPatientId());
        PatientTrackingProfile profile = profileOpt.orElseGet(() -> {
            PatientTrackingProfile newProf = new PatientTrackingProfile(event.getPatientId(), "Unknown Patient", "N/A");
            return profileRepository.save(newProf);
        });

        profile.setTotalPrescriptions(profile.getTotalPrescriptions() + 1);
        profile.setLastActivity(LocalDateTime.now());

        // AI Rule-based risk analysis
        if (profile.getTotalPrescriptions() >= 5) {
            profile.setRiskStatus("HIGH");
            createInsight(event.getPatientId(), "CROSS_DEPARTMENT", "ADHERENCE_RISK",
                    "AI Alert: Patient has " + profile.getTotalPrescriptions() + " active prescriptions. High risk of drug interaction or polypharmacy.", "HIGH");
        } else if (profile.getTotalPrescriptions() >= 3) {
            profile.setRiskStatus("MEDIUM");
            createInsight(event.getPatientId(), "CROSS_DEPARTMENT", "ADHERENCE_RISK",
                    "AI Notice: Patient prescription count elevated (" + profile.getTotalPrescriptions() + "). Monitoring dosage compliance.", "MEDIUM");
        } else {
            createInsight(event.getPatientId(), "PHARMACY_DEPARTMENT", "PRESCRIPTION_DISPENSED",
                    "AI tracked prescription for medication '" + event.getMedicationName() + "' (Quantity: " + event.getQuantity() + ", Status: " + event.getStatus() + ").", "LOW");
        }

        profileRepository.save(profile);
    }

    @Transactional
    public void processPharmacyInventoryEvent(MedicationInventoryEvent event) {
        boolean belowReorderPoint = event.getCurrentStock() <= event.getReorderThreshold();
        boolean crossedReorderPoint = event.getPreviousStock() > event.getReorderThreshold() && belowReorderPoint;
        boolean createdBelowReorderPoint = "CREATED".equals(event.getChangeType()) && belowReorderPoint;
        boolean newlyOutOfStock = event.getCurrentStock() == 0 && event.getPreviousStock() > 0;
        if (!crossedReorderPoint && !createdBelowReorderPoint && !newlyOutOfStock) return;

        String severity = event.getCurrentStock() == 0 ? "CRITICAL" : "MEDIUM";
        String message = event.getCurrentStock() == 0
                ? "Medication '" + event.getMedicationName() + "' is out of stock. Reorder threshold: " + event.getReorderThreshold() + "."
                : "Medication '" + event.getMedicationName() + "' is at its reorder point (" + event.getCurrentStock()
                    + " remaining; threshold " + event.getReorderThreshold() + ").";
        createInsight(null, "PHARMACY_DEPARTMENT", "LOW_STOCK", message, severity);
        log.info("[PHARMACY AI] Inventory alert for medication {} at stock {} / threshold {}",
                event.getMedicationId(), event.getCurrentStock(), event.getReorderThreshold());
    }

    private void createInsight(String patientId, String department, String type, String message, String severity) {
        AiInsightLog logEntry = new AiInsightLog(null, patientId, department, type, message, severity, LocalDateTime.now());
        insightLogRepository.save(logEntry);
    }

    public List<AiInsightLog> getAllInsights() {
        return insightLogRepository.findAll();
    }

    public List<PatientTrackingProfile> getPatientRiskProfiles() {
        return profileRepository.findAll();
    }

    public Map<String, Object> getAiDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalTrackedPatients", profileRepository.count());
        metrics.put("totalAiInsightsGenerated", insightLogRepository.count());
        metrics.put("highRiskPatients", profileRepository.findByRiskStatus("HIGH").size());
        metrics.put("mediumRiskPatients", profileRepository.findByRiskStatus("MEDIUM").size());
        metrics.put("status", "AI Tracking Active - Monitoring Patient & Pharmacy Departments");
        return metrics;
    }
}
