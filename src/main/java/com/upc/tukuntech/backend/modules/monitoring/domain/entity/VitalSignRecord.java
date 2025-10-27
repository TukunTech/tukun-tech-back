package com.upc.tukuntech.backend.modules.monitoring.domain.entity;

import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.HeartRate;
import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.OxygenLevel;
import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.Temperature;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "vital_sign_records")
@Getter
public class VitalSignRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long deviceId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "heart_rate", nullable = false))
    })
    private HeartRate heartRate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "oxygen_level", nullable = false))
    })
    private OxygenLevel oxygenLevel;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "temperature", nullable = false))
    })
    private Temperature temperature;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected VitalSignRecord() {}

    public static VitalSignRecord create(
            Long patientId, Long deviceId, HeartRate hr, OxygenLevel spo2, Temperature temp) {
        if (patientId == null || deviceId == null)
            throw new IllegalArgumentException("patientId/deviceId required");

        VitalSignRecord r = new VitalSignRecord();
        r.patientId = patientId;
        r.deviceId  = deviceId;
        r.heartRate = hr;
        r.oxygenLevel = spo2;
        r.temperature = temp;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    public boolean isAbnormal() {
        return heartRate.isAbnormal() || oxygenLevel.isAbnormal() || temperature.isAbnormal();
    }

    /** Produce un Alert básico si hay anormalidad. */
    public Optional<Alert> generateAlertIfNeeded() {
        if (!isAbnormal()) return Optional.empty();

        StringBuilder msg = new StringBuilder("Valores fuera de rango → ");
        if (heartRate.isAbnormal())   msg.append("HR=").append(heartRate.getValue()).append(" bpm | ");
        if (oxygenLevel.isAbnormal()) msg.append("SpO2=").append(oxygenLevel.getValue()).append("% | ");
        if (temperature.isAbnormal()) msg.append("Temp=").append(temperature.getValue()).append(" °C");

        Alert alert = Alert.createCritical(patientId, deviceId, timestamp, msg.toString().trim());
        return Optional.of(alert);
    }
}
