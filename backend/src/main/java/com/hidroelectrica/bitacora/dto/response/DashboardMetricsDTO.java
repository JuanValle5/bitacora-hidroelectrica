package com.hidroelectrica.bitacora.dto.response;

import java.time.LocalDate;

public record DashboardMetricsDTO(
    LocalDate date,
    double totalGenerationMwh,
    double totalGenerationKwh,
    double averagePowerMw,
    String activeShiftLabel,
    String activeShiftRange,
    int completedHours,
    int totalHours,
    double completionPercentage
) {}
