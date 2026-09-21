package com.hidroelectrica.bitacora.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DailyReportDTO(
    Long reportId,
    LocalDate date,
    String status,
    int savedHoursCount,
    double totalGenBrutaKwh,
    double totalKwhG1,
    double totalKwhG2,
    double totalKwhCombined,
    List<HourlyReadingDTO> readings
) {}
