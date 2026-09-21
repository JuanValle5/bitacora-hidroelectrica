package com.hidroelectrica.bitacora.service;

import com.hidroelectrica.bitacora.exception.ResourceNotFoundException;
import com.hidroelectrica.bitacora.model.DailyReport;
import com.hidroelectrica.bitacora.model.HourlyReading;
import com.hidroelectrica.bitacora.repository.DailyReportRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PdfExportService {

    private final DailyReportRepository reportRepository;

    public PdfExportService(DailyReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public byte[] generateDailyReportPdf(LocalDate date) {
        DailyReport report = reportRepository.findByReportDate(date)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró reporte para la fecha " + date));

        return buildPdf(report);
    }

    private byte[] buildPdf(DailyReport report) {
        StringBuilder streamContent = new StringBuilder();
        streamContent.append("BT\n");
        streamContent.append("/F1 15 Tf\n");
        streamContent.append("40 760 Td\n");
        streamContent.append("(CENTRAL HIDROELECTRICA - BITACORA OPERATIVA DIARIA) Tj\n");
        streamContent.append("0 -20 Td\n");
        streamContent.append("/F1 10 Tf\n");
        streamContent.append("(Fecha de Operacion: ").append(report.getReportDate()).append(" | Estado: ").append(report.getStatus()).append(") Tj\n");
        streamContent.append("0 -15 Td\n");
        streamContent.append("(Reporte oficial emitido para trazabilidad, control y auditoria de planta) Tj\n");
        streamContent.append("0 -24 Td\n");

        streamContent.append("/F1 8 Tf\n");
        streamContent.append("(HORA | T.CARGA | T.DESC | S.AUX | EPSA ACTARIS | GEN.BRUTA | POT G1 | KWH G1 | POT G2 | KWH G2 | EDIT | OBSERVACIONES) Tj\n");
        streamContent.append("0 -10 Td\n");
        streamContent.append("(-----------------------------------------------------------------------------------------------------------------------) Tj\n");

        List<HourlyReading> sorted = new ArrayList<>(report.getReadings());
        sorted.sort(Comparator.comparingInt(HourlyReading::getHour));

        double totalGenBruta = 0;
        double totalG1 = 0;
        double totalG2 = 0;

        for (HourlyReading r : sorted) {
            String cCarga = r.getNivelCarga() != null ? String.format("%.2f", r.getNivelCarga()) : "—";
            String cDesc = r.getNivelDescarga() != null ? String.format("%.2f", r.getNivelDescarga()) : "—";
            String sAux = r.getServAuxKwh() != null ? String.format("%.1f", r.getServAuxKwh()) : "—";
            String epsa = r.getEpsaActarisKwh() != null ? String.format("%.2f", r.getEpsaActarisKwh()) : "—";
            String genB = r.getGenBrutaKwh() != null ? String.format("%.0f", r.getGenBrutaKwh()) : "—";
            String pot1 = r.getPotActivaG1() != null ? String.format("%.0f", r.getPotActivaG1()) : "—";
            String kwh1 = r.getKwhG1() != null ? String.format("%.0f", r.getKwhG1()) : "—";
            String pot2 = r.getPotActivaG2() != null ? String.format("%.0f", r.getPotActivaG2()) : "—";
            String kwh2 = r.getKwhG2() != null ? String.format("%.0f", r.getKwhG2()) : "—";
            String edit = Boolean.TRUE.equals(r.getIsEdited()) ? "SI" : "NO";

            if (r.getGenBrutaKwh() != null) totalGenBruta += r.getGenBrutaKwh();
            if (r.getKwhG1() != null) totalG1 += r.getKwhG1();
            if (r.getKwhG2() != null) totalG2 += r.getKwhG2();

            String obs = r.getObservations() != null ? r.getObservations().replace("(", "").replace(")", "").replace("\\", "") : "";
            if (obs.length() > 22) obs = obs.substring(0, 19) + "...";

            String line = String.format("%02d:00 | %7s | %6s | %5s | %12s | %9s | %6s | %6s | %6s | %6s | %4s | %s",
                r.getHour(), cCarga, cDesc, sAux, epsa, genB, pot1, kwh1, pot2, kwh2, edit, obs
            );

            streamContent.append("0 -13 Td\n");
            streamContent.append("(").append(line).append(") Tj\n");
        }

        streamContent.append("0 -12 Td\n");
        streamContent.append("(-----------------------------------------------------------------------------------------------------------------------) Tj\n");
        streamContent.append("0 -14 Td\n");
        streamContent.append(String.format("(TOTALES DIA: Generacion Bruta: %.0f kWh | Generacion G1: %.0f kWh | Generacion G2: %.0f kWh) Tj\n",
            totalGenBruta, totalG1, totalG2
        ));
        streamContent.append("0 -24 Td\n");
        streamContent.append("(Firma Operador Turno A: ________________________   Firma Operador Turno B: ________________________) Tj\n");

        streamContent.append("ET\n");

        byte[] streamBytes = streamContent.toString().getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            List<Long> offsets = new ArrayList<>();
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 1: Catalog
            offsets.add((long) out.size());
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 2: Pages
            offsets.add((long) out.size());
            out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 3: Page (Letter landscape or large portrait: 612 x 792)
            offsets.add((long) out.size());
            out.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 4: Font
            offsets.add((long) out.size());
            out.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Obj 5: Content Stream
            offsets.add((long) out.size());
            out.write(("5 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.US_ASCII));
            out.write(streamBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Xref table
            long startXref = out.size();
            out.write("xref\n0 6\n0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            for (Long offset : offsets) {
                out.write(String.format("%010d 00000 n \n", offset).getBytes(StandardCharsets.US_ASCII));
            }

            // Trailer
            out.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + startXref + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));
            return out.toByteArray();
        } catch (IOException e) {
            return streamBytes;
        }
    }
}
