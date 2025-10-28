package com.upc.tukuntech.backend.modules.reports.domain.repositories;

import com.upc.tukuntech.backend.modules.reports.domain.entity.DailyReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository {

    Optional<DailyReport> findByPatientIdAndDate(Long patientId, LocalDate date);

    List<DailyReport> findByPatientId(Long patientId);

    DailyReport save(DailyReport report);
}
