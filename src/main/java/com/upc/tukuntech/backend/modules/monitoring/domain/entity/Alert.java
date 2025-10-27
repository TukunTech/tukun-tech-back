package com.upc.tukuntech.backend.modules.monitoring.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long patientId;
    @Column(nullable = false) private Long deviceId;
    @Column(nullable = false) private String severity; // CRITICAL / WARNING / INFO
    @Column(nullable = false) private String message;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = true)  private String type;     // TEMPERATURE / HEART_RATE / OXYGENATION

    // --- Factory Methods ---
    public static Alert createCritical(Long patientId, Long deviceId, LocalDateTime ts, String message) {
        Alert a = new Alert();
        a.patientId = patientId;
        a.deviceId = deviceId;
        a.createdAt = ts != null ? ts : LocalDateTime.now();
        a.severity = "CRITICAL";
        a.message = message;
        a.type = inferTypeFromMessage(message);
        return a;
    }

    // --- Helper to infer type automatically ---
    private static String inferTypeFromMessage(String message) {
        if (message == null) return "GENERAL";
        String lower = message.toLowerCase();
        if (lower.contains("temp")) return "TEMPERATURE";
        if (lower.contains("spo2")) return "OXYGENATION";
        if (lower.contains("hr")) return "HEART_RATE";
        return "GENERAL";
    }
}
