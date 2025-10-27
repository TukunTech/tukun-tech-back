package com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class HeartRate {
    @Column(name = "heart_rate", nullable = false)
    private Integer value;

    protected HeartRate() {}

    public HeartRate(Integer value) {
        if (value == null || value < 30 || value > 220)
            throw new IllegalArgumentException("Heart rate out of sensor range (30–220 bpm).");
        this.value = value;
    }

    public boolean isAbnormal() {
        return value < 50 || value > 110;
    }

    public Integer getValue() { return value; }
}
