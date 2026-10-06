package com.pm.aiservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_insight_logs")
public class AiInsightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String patientId;

    @Column(nullable = false)
    private String department; // PATIENT_DEPARTMENT, PHARMACY_DEPARTMENT, CROSS_DEPARTMENT

    @Column(nullable = false)
    private String insightType; // ANOMALY, ADHERENCE_RISK, INVENTORY_FORECAST, SAFETY_WARNING

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private LocalDateTime timestamp;

    public AiInsightLog() {}

    public AiInsightLog(UUID id, String patientId, String department, String insightType, String message, String severity, LocalDateTime timestamp) {
        this.id = id;
        this.patientId = patientId;
        this.department = department;
        this.insightType = insightType;
        this.message = message;
        this.severity = severity;
        this.timestamp = timestamp;
    }

    @PrePersist
    public void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getInsightType() { return insightType; }
    public void setInsightType(String insightType) { this.insightType = insightType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
