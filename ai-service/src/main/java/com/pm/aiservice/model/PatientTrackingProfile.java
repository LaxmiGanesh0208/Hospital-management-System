package com.pm.aiservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_tracking_profiles")
public class PatientTrackingProfile {

    @Id
    private String patientId;

    private String patientName;

    private String patientEmail;

    private Integer totalPrescriptions = 0;

    private String riskStatus = "LOW"; // LOW, MEDIUM, HIGH

    private LocalDateTime lastActivity;

    public PatientTrackingProfile() {}

    public PatientTrackingProfile(String patientId, String patientName, String patientEmail) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.lastActivity = LocalDateTime.now();
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public Integer getTotalPrescriptions() { return totalPrescriptions; }
    public void setTotalPrescriptions(Integer totalPrescriptions) { this.totalPrescriptions = totalPrescriptions; }

    public String getRiskStatus() { return riskStatus; }
    public void setRiskStatus(String riskStatus) { this.riskStatus = riskStatus; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
}
