package com.pm.aiservice.service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Synthetic data for local UI demonstrations only. Never connected to medical equipment. */
@Service
public class DemoMonitoringService {
    private final boolean enabled;
    private final AtomicLong sequence = new AtomicLong();
    private final ArrayDeque<Map<String, Object>> alerts = new ArrayDeque<>();
    private List<Map<String, Object>> latestReadings = List.of();
    private boolean demoScenarioAlerted;

    public DemoMonitoringService(@Value("${app.demo-monitoring.enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    @Scheduled(fixedRateString = "${app.demo-monitoring.interval-ms:5000}")
    public synchronized void generateDemoReadings() {
        if (!enabled) return;
        long tick = sequence.incrementAndGet();
        Instant now = Instant.now();
        int heartRate = 76 + (int) (Math.sin(tick / 3.0) * 4);
        int oxygen = 97 + (int) (Math.sin(tick / 4.0) * 1);
        latestReadings = List.of(
                reading("DEMO-ICU-01", "Demo Patient A", "ICU", heartRate, oxygen, "NORMAL", now),
                reading("DEMO-WARD-02", "Demo Patient B", "Ward", 84, 96, "NORMAL", now));

        // A predictable synthetic event exercises the alert UI once per demo run.
        if (tick >= 6 && !demoScenarioAlerted) {
            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("id", "DEMO-ALERT-" + tick);
            alert.put("patientId", "DEMO-ICU-01");
            alert.put("patientName", "Demo Patient A");
            alert.put("department", "ICU");
            alert.put("severity", "DEMO HIGH");
            alert.put("message", "Synthetic monitor alarm scenario for testing the staff alert screen.");
            alert.put("createdAt", now.toString());
            alert.put("delivery", "IN-APP DEMO ONLY — no message sent");
            alerts.addFirst(alert);
            while (alerts.size() > 10) alerts.removeLast();
            demoScenarioAlerted = true;
        }
    }

    public synchronized Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", enabled);
        result.put("simulated", true);
        result.put("notice", "Synthetic demo data only. Not connected to equipment, not clinical monitoring, and not for care decisions.");
        result.put("updatedAt", latestReadings.isEmpty() ? null : latestReadings.get(0).get("timestamp"));
        result.put("readings", latestReadings);
        result.put("alerts", new ArrayList<>(alerts));
        result.put("evaluation", "Demonstration rule only; no trained AI model.");
        return result;
    }

    private static Map<String, Object> reading(String id, String name, String location,
                                                int heartRate, int oxygen, String status, Instant at) {
        Map<String, Object> reading = new LinkedHashMap<>();
        reading.put("patientId", id);
        reading.put("patientName", name);
        reading.put("location", location);
        reading.put("heartRateBpm", heartRate);
        reading.put("oxygenSaturationPercent", oxygen);
        reading.put("status", status);
        reading.put("timestamp", at.toString());
        return reading;
    }
}
