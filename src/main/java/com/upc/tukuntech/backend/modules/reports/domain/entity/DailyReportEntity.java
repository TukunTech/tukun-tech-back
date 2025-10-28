package com.upc.tukuntech.backend.modules.reports.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_reports")
@Getter
@Setter
public class DailyReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private LocalDate date;
    private Double avgHeartRate;
    private Double avgOxygenLevel;
    private Double avgTemperature;
    private Integer totalAlerts;
}
