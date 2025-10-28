package com.upc.tukuntech.backend.modules.reports.infrastructure.persistence;

import com.upc.tukuntech.backend.modules.reports.domain.entity.DailyReport;
import com.upc.tukuntech.backend.modules.reports.domain.repositories.DailyReportRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaDailyReportRepository
        extends DailyReportRepository, JpaRepository<DailyReport, Long> {

    @Override
    Optional<DailyReport> findByPatientIdAndDate(Long patientId, LocalDate date);

    @Override
    List<DailyReport> findByPatientId(Long patientId);
}
