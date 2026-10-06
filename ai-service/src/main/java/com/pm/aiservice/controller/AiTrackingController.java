package com.pm.aiservice.controller;

import com.pm.aiservice.model.AiInsightLog;
import com.pm.aiservice.model.PatientTrackingProfile;
import com.pm.aiservice.service.AiEngineService;
import com.pm.aiservice.service.DemoMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI Department", description = "AI Intelligence & Analytics tracking Patient and Pharmacy Departments")
public class AiTrackingController {

    private final AiEngineService aiEngineService;
    private final DemoMonitoringService demoMonitoringService;

    public AiTrackingController(AiEngineService aiEngineService, DemoMonitoringService demoMonitoringService) {
        this.aiEngineService = aiEngineService;
        this.demoMonitoringService = demoMonitoringService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get cross-departmental AI dashboard metrics")
    public ResponseEntity<Map<String, Object>> getAiDashboard(
            @RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
        requireAdmin(role);
        return ResponseEntity.ok(aiEngineService.getAiDashboardMetrics());
    }

    @GetMapping("/insights")
    @Operation(summary = "Get all AI-generated safety alerts and tracking insights")
    public ResponseEntity<List<AiInsightLog>> getInsights(
            @RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
        requireAdmin(role);
        return ResponseEntity.ok(aiEngineService.getAllInsights());
    }

    @GetMapping("/patient-profiles")
    @Operation(summary = "Get AI-monitored patient risk profiles")
    public ResponseEntity<List<PatientTrackingProfile>> getPatientProfiles(
            @RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
        requireAdmin(role);
        return ResponseEntity.ok(aiEngineService.getPatientRiskProfiles());
    }

    @GetMapping("/demo-monitoring")
    @Operation(summary = "Get synthetic patient monitoring data for local UI demonstrations")
    public ResponseEntity<Map<String, Object>> getDemoMonitoring(
            @RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
        requireAdmin(role);
        return ResponseEntity.ok(demoMonitoringService.snapshot());
    }

    private static void requireAdmin(String role) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator role required");
    }
}
