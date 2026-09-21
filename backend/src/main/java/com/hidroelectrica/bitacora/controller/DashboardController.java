package com.hidroelectrica.bitacora.controller;

import com.hidroelectrica.bitacora.dto.response.DashboardMetricsDTO;
import com.hidroelectrica.bitacora.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDTO> getMetrics(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate reportDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(dashboardService.getMetrics(reportDate));
    }
}
