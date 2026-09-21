package com.hidroelectrica.bitacora;

import com.hidroelectrica.bitacora.model.DailyReport;
import com.hidroelectrica.bitacora.model.HourlyReading;
import com.hidroelectrica.bitacora.repository.DailyReportRepository;
import com.hidroelectrica.bitacora.service.PdfExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportServiceTest {

    @Mock
    private DailyReportRepository reportRepository;

    @InjectMocks
    private PdfExportService pdfExportService;

    @Test
    @DisplayName("Debe generar flujo binario PDF con firma %PDF-1.4 válida")
    void testGeneratePdf() {
        LocalDate date = LocalDate.of(2026, 9, 20);
        DailyReport report = new DailyReport(date, null);
        HourlyReading reading = new HourlyReading(10);
        reading.setSaved(true);
        reading.setNivelCarga(646.30);
        reading.setPotActivaG1(6200.0);
        report.addReading(reading);

        when(reportRepository.findByReportDate(date)).thenReturn(Optional.of(report));

        byte[] pdfBytes = pdfExportService.generateDailyReportPdf(date);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 100);

        String pdfStart = new String(pdfBytes, 0, 8, StandardCharsets.US_ASCII);
        assertTrue(pdfStart.startsWith("%PDF-1.4"));
    }
}
