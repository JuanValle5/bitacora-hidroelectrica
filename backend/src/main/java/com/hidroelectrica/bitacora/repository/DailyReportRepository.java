package com.hidroelectrica.bitacora.repository;

import com.hidroelectrica.bitacora.model.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
    Optional<DailyReport> findByReportDate(LocalDate reportDate);
    boolean existsByReportDate(LocalDate reportDate);
}
