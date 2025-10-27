package com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Temperature {
    @Column(name = "temperature", nullable = false)
    private Double value;

    protected Temperature() {}

    public Temperature(Double value) {
        if (value == null || value < 30.0 || value > 45.0)
            throw new IllegalArgumentException("Temperature out of sensor range (30–45°C).");
        // redondeo a 0.1 para ruidos del sensor
        this.value = Math.round(value * 10d) / 10d;
    }

    public boolean isAbnormal() {
        return value < 35.0 || value > 38.0;
    }

    public Double getValue() { return value; }
}
