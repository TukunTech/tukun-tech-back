package com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OxygenLevel {
    @Column(name = "oxygen_level", nullable = false)
    private Integer value;

    protected OxygenLevel() {}

    public OxygenLevel(Integer value) {
        if (value == null || value < 50 || value > 100)
            throw new IllegalArgumentException("SpO2 out of sensor range (50–100%).");
        this.value = value;
    }

    public boolean isAbnormal() {
        return value < 90;
    }

    public Integer getValue() { return value; }
}
