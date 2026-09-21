package com.hidroelectrica.bitacora.repository;

import com.hidroelectrica.bitacora.model.HourlyReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyReadingRepository extends JpaRepository<HourlyReading, Long> {
    Optional<HourlyReading> findByDailyReportIdAndHour(Long dailyReportId, Integer hour);
    List<HourlyReading> findByDailyReportIdOrderByHourAsc(Long dailyReportId);
}
