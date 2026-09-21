package com.hidroelectrica.bitacora.service;

import com.hidroelectrica.bitacora.dto.response.DailyReportDTO;
import com.hidroelectrica.bitacora.dto.response.DashboardMetricsDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardService {

    private final DailyReportService dailyReportService;
    private final ShiftService shiftService;

    public DashboardService(DailyReportService dailyReportService, ShiftService shiftService) {
        this.dailyReportService = dailyReportService;
        this.shiftService = shiftService;
    }

    public DashboardMetricsDTO getMetrics(LocalDate date) {
        DailyReportDTO report = dailyReportService.getReportByDate(date);
        ShiftService.ShiftInfo shift = shiftService.getCurrentShift();

        double totalKwh = report.totalKwhCombined();
        double totalMwh = Math.round((totalKwh / 1000.0) * 100.0) / 100.0;
        int completed = report.savedHoursCount();
        double avgMw = completed > 0 ? Math.round((totalMwh / completed) * 100.0) / 100.0 : 0.0;
        double percentage = Math.round(((double) completed / 24.0 * 100.0) * 10.0) / 10.0;

        return new DashboardMetricsDTO(
            date,
            totalMwh,
            totalKwh,
            avgMw,
            shift.label(),
            shift.range(),
            completed,
            24,
            percentage
        );
    }
}
