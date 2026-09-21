package com.hidroelectrica.bitacora.controller;

import com.hidroelectrica.bitacora.dto.request.HourlyReadingCreateDTO;
import com.hidroelectrica.bitacora.dto.request.HourlyReadingUpdateDTO;
import com.hidroelectrica.bitacora.dto.response.DailyReportDTO;
import com.hidroelectrica.bitacora.dto.response.HourlyReadingDTO;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.security.UserContext;
import com.hidroelectrica.bitacora.service.DailyReportService;
import com.hidroelectrica.bitacora.service.PdfExportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/daily-reports")
public class DailyReportController {

    private final DailyReportService dailyReportService;
    private final PdfExportService pdfExportService;

    public DailyReportController(DailyReportService dailyReportService, PdfExportService pdfExportService) {
        this.dailyReportService = dailyReportService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping
    public ResponseEntity<DailyReportDTO> getReport(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate reportDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(dailyReportService.getReportByDate(reportDate));
    }

    @PostMapping("/{date}/readings")
    public ResponseEntity<HourlyReadingDTO> registerReading(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Valid @RequestBody HourlyReadingCreateDTO dto
    ) {
        User currentUser = UserContext.getCurrentUser();
        HourlyReadingDTO saved = dailyReportService.registerReading(date, dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{date}/readings/{hour}")
    public ResponseEntity<HourlyReadingDTO> updateReading(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @PathVariable Integer hour,
        @Valid @RequestBody HourlyReadingUpdateDTO dto
    ) {
        User currentUser = UserContext.getCurrentUser();
        HourlyReadingDTO updated = dailyReportService.updateReading(date, hour, dto, currentUser);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{date}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        byte[] pdfBytes = pdfExportService.generateDailyReportPdf(date);
        String filename = "bitacora-hidroelectrica-" + date + ".pdf";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }
}
