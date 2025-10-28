package com.upc.tukuntech.backend.modules.reports.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_reports")
@Getter
@Setter
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double avgHeartRate;

    @Column(nullable = false)
    private Double avgOxygenLevel;

    @Column(nullable = false)
    private Double avgTemperature;

    @Column(nullable = false)
    private Integer totalAlerts;

    protected DailyReport() {}

    private DailyReport(Long patientId, LocalDate date,
                        Double avgHeartRate, Double avgOxygenLevel,
                        Double avgTemperature, Integer totalAlerts) {
        this.patientId = patientId;
        this.date = date;
        this.avgHeartRate = avgHeartRate;
        this.avgOxygenLevel = avgOxygenLevel;
        this.avgTemperature = avgTemperature;
        this.totalAlerts = totalAlerts;
    }

    public static DailyReport create(Long patientId, LocalDate date,
                                     Double avgHeartRate, Double avgOxygenLevel,
                                     Double avgTemperature, Integer totalAlerts) {
        return new DailyReport(patientId, date, avgHeartRate, avgOxygenLevel, avgTemperature, totalAlerts);
    }
}
