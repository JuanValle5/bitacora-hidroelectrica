package com.hidroelectrica.bitacora.service;

import com.hidroelectrica.bitacora.dto.request.HourlyReadingCreateDTO;
import com.hidroelectrica.bitacora.dto.request.HourlyReadingUpdateDTO;
import com.hidroelectrica.bitacora.dto.response.DailyReportDTO;
import com.hidroelectrica.bitacora.dto.response.HourlyReadingDTO;
import com.hidroelectrica.bitacora.exception.BusinessRuleException;
import com.hidroelectrica.bitacora.exception.ResourceNotFoundException;
import com.hidroelectrica.bitacora.model.DailyReport;
import com.hidroelectrica.bitacora.model.HourlyReading;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.DailyReportRepository;
import com.hidroelectrica.bitacora.repository.HourlyReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class DailyReportService {

    private final DailyReportRepository reportRepository;
    private final HourlyReadingRepository readingRepository;
    private final CalculationEngineService calculationEngine;
    private final AuditService auditService;
    private final ShiftService shiftService;

    public DailyReportService(
        DailyReportRepository reportRepository,
        HourlyReadingRepository readingRepository,
        CalculationEngineService calculationEngine,
        AuditService auditService,
        ShiftService shiftService
    ) {
        this.reportRepository = reportRepository;
        this.readingRepository = readingRepository;
        this.calculationEngine = calculationEngine;
        this.auditService = auditService;
        this.shiftService = shiftService;
    }

    @Transactional
    public DailyReport getOrCreateDailyReportEntity(LocalDate date, User user) {
        return reportRepository.findByReportDate(date).orElseGet(() -> {
            DailyReport report = new DailyReport(date, user);
            for (int h = 0; h < 24; h++) {
                HourlyReading reading = new HourlyReading(h);
                report.addReading(reading);
            }
            return reportRepository.save(report);
        });
    }

    @Transactional
    public DailyReportDTO getReportByDate(LocalDate date) {
        DailyReport report = getOrCreateDailyReportEntity(date, null);
        return toDTO(report);
    }

    @Transactional
    public HourlyReadingDTO registerReading(LocalDate date, HourlyReadingCreateDTO dto, User user) {
        validateUserInShift(user);

        // Validar regla de datos faltantes: si hay campos nulos y no hay observaciones -> error
        if (hasEmptyMeasurementFields(dto)) {
            if (dto.observations() == null || dto.observations().trim().length() < 3) {
                throw new BusinessRuleException("Debe justificar en las observaciones el motivo por el cual hay campos de lectura sin registrar.");
            }
        }

        DailyReport report = getOrCreateDailyReportEntity(date, user);
        HourlyReading reading = readingRepository.findByDailyReportIdAndHour(report.getId(), dto.hour())
            .orElseThrow(() -> new ResourceNotFoundException("Hora " + dto.hour() + " no encontrada en el reporte"));

        // Asignar valores
        copyDtoToEntity(dto, reading);
        reading.setSaved(true);
        reading.setLastModifiedBy(user);

        // Calcular derivadas con respecto a hora anterior
        HourlyReading prev = getPreviousReading(report, dto.hour());
        applyCalculations(reading, prev);

        reading = readingRepository.save(reading);

        // Si la hora siguiente ya estaba registrada, recalcular sus derivadas en cascada
        recalculateDownstream(report, dto.hour());

        return toReadingDTO(reading);
    }

    @Transactional
    public HourlyReadingDTO updateReading(LocalDate date, Integer hour, HourlyReadingUpdateDTO dto, User user) {
        validateUserInShift(user);

        if (dto.justification() == null || dto.justification().trim().length() < 10) {
            throw new BusinessRuleException("La justificación del cambio es obligatoria y debe contener al menos 10 caracteres.");
        }

        DailyReport report = reportRepository.findByReportDate(date)
            .orElseThrow(() -> new ResourceNotFoundException("No existe reporte para la fecha " + date));

        HourlyReading reading = readingRepository.findByDailyReportIdAndHour(report.getId(), hour)
            .orElseThrow(() -> new ResourceNotFoundException("Hora " + hour + " no encontrada en el reporte"));

        // Detectar cambios y auditar
        detectAndAuditChanges(reading, dto, user, date, hour);

        // Actualizar valores en la entidad
        applyUpdateDtoToEntity(dto, reading);
        reading.setSaved(true);
        reading.setIsEdited(true);
        reading.setLastModifiedBy(user);

        // Recalcular métricas de la hora
        HourlyReading prev = getPreviousReading(report, hour);
        applyCalculations(reading, prev);

        reading = readingRepository.save(reading);

        // Recalcular hora siguiente si aplica
        recalculateDownstream(report, hour);

        return toReadingDTO(reading);
    }

    private void applyCalculations(HourlyReading current, HourlyReading previous) {
        if (previous != null) {
            current.setGenBrutaKwh(calculationEngine.calculateGenBrutaKwh(current.getEpsaActarisKwh(), previous.getEpsaActarisKwh()));
            current.setKwhG1(calculationEngine.calculateKwhG1(current.getContActarisG1(), previous.getContActarisG1()));
            current.setKwhG2(calculationEngine.calculateKwhG2(current.getContActarisG2(), previous.getContActarisG2()));
        } else {
            current.setGenBrutaKwh(null);
            current.setKwhG1(null);
            current.setKwhG2(null);
        }
    }

    private void recalculateDownstream(DailyReport report, int hour) {
        HourlyReading nextSaved = null;
        if (report.getReadings() != null && !report.getReadings().isEmpty()) {
            for (int h = hour + 1; h < 24; h++) {
                final int targetHour = h;
                HourlyReading candidate = report.getReadings().stream()
                    .filter(r -> r.getHour() == targetHour && Boolean.TRUE.equals(r.getSaved()))
                    .findFirst()
                    .orElse(null);
                if (candidate != null) {
                    nextSaved = candidate;
                    break;
                }
            }
        }
        if (nextSaved == null) {
            for (int h = hour + 1; h < 24; h++) {
                Optional<HourlyReading> opt = readingRepository.findByDailyReportIdAndHour(report.getId(), h);
                if (opt.isPresent() && Boolean.TRUE.equals(opt.get().getSaved())) {
                    nextSaved = opt.get();
                    break;
                }
            }
        }

        if (nextSaved != null) {
            HourlyReading prevForNext = getPreviousReading(report, nextSaved.getHour());
            applyCalculations(nextSaved, prevForNext);
            readingRepository.save(nextSaved);
        } else if (hour == 23) {
            LocalDate nextDate = report.getReportDate().plusDays(1);
            reportRepository.findByReportDate(nextDate).ifPresent(nextReport -> {
                for (int h = 0; h < 24; h++) {
                    final int targetHour = h;
                    HourlyReading firstCandidate = null;
                    if (nextReport.getReadings() != null && !nextReport.getReadings().isEmpty()) {
                        firstCandidate = nextReport.getReadings().stream()
                            .filter(r -> r.getHour() == targetHour && Boolean.TRUE.equals(r.getSaved()))
                            .findFirst()
                            .orElse(null);
                    }
                    if (firstCandidate == null) {
                        firstCandidate = readingRepository.findByDailyReportIdAndHour(nextReport.getId(), targetHour)
                            .filter(r -> Boolean.TRUE.equals(r.getSaved()))
                            .orElse(null);
                    }
                    if (firstCandidate != null) {
                        HourlyReading prev = getPreviousReading(nextReport, firstCandidate.getHour());
                        applyCalculations(firstCandidate, prev);
                        readingRepository.save(firstCandidate);
                        break;
                    }
                }
            });
        }
    }

    private HourlyReading getPreviousReading(DailyReport report, int currentHour) {
        // 1. Buscar hacia atrás en el mismo día
        if (currentHour > 0) {
            if (report.getReadings() != null && !report.getReadings().isEmpty()) {
                for (int h = currentHour - 1; h >= 0; h--) {
                    final int targetHour = h;
                    HourlyReading candidate = report.getReadings().stream()
                        .filter(r -> r.getHour() == targetHour && Boolean.TRUE.equals(r.getSaved()) && hasCounters(r))
                        .findFirst()
                        .orElse(null);
                    if (candidate != null) return candidate;
                }
            }
            for (int h = currentHour - 1; h >= 0; h--) {
                Optional<HourlyReading> prevOpt = readingRepository.findByDailyReportIdAndHour(report.getId(), h);
                if (prevOpt.isPresent()) {
                    HourlyReading prev = prevOpt.get();
                    if (Boolean.TRUE.equals(prev.getSaved()) && hasCounters(prev)) {
                        return prev;
                    }
                }
            }
        }

        // 2. Si no se encontró en el día actual (o es hora 0), buscar en el día anterior
        LocalDate prevDate = report.getReportDate().minusDays(1);
        Optional<DailyReport> prevReportOpt = reportRepository.findByReportDate(prevDate);
        if (prevReportOpt.isPresent()) {
            DailyReport prevReport = prevReportOpt.get();
            if (prevReport.getReadings() != null && !prevReport.getReadings().isEmpty()) {
                for (int h = 23; h >= 0; h--) {
                    final int targetHour = h;
                    HourlyReading candidate = prevReport.getReadings().stream()
                        .filter(r -> r.getHour() == targetHour && Boolean.TRUE.equals(r.getSaved()) && hasCounters(r))
                        .findFirst()
                        .orElse(null);
                    if (candidate != null) return candidate;
                }
            }
            for (int h = 23; h >= 0; h--) {
                Optional<HourlyReading> prevOpt = readingRepository.findByDailyReportIdAndHour(prevReport.getId(), h);
                if (prevOpt.isPresent()) {
                    HourlyReading prev = prevOpt.get();
                    if (Boolean.TRUE.equals(prev.getSaved()) && hasCounters(prev)) {
                        return prev;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasCounters(HourlyReading r) {
        return r.getEpsaActarisKwh() != null || r.getContActarisG1() != null || r.getContActarisG2() != null;
    }

    private boolean hasEmptyMeasurementFields(HourlyReadingCreateDTO d) {
        return d.nivelCarga() == null || d.nivelDescarga() == null
            || d.servAuxKwh() == null || d.epsaActarisKwh() == null
            || d.potActivaG1() == null || d.voltExcG1() == null || d.corrExcG1() == null
            || d.voltG1rst() == null || d.corrG1faseR() == null || d.corrG1faseS() == null || d.corrG1faseT() == null
            || d.contActarisG1() == null || d.tempTrafoF1() == null || d.tempTrafoF2() == null || d.tempTrafoF3() == null
            || d.tempG1CojExc() == null || d.tempG1SalidaAire() == null || d.tempG1EntradaAire() == null
            || d.tempG1CojAcoplado() == null || d.tempG1CojNoAcoplado() == null || d.tempG1CojEmpuje() == null
            || d.tempG1Aceite() == null || d.tempG1SalidaAireExc() == null
            || d.potActivaG2() == null || d.voltExcG2() == null || d.corrExcG2() == null
            || d.voltG2rst() == null || d.corrG2faseR() == null || d.corrG2faseS() == null || d.corrG2faseT() == null
            || d.tempCojGuiaG2() == null || d.tempCojAcopladoT2() == null || d.contActarisG2() == null
            || d.tempG2CojExc() == null || d.tempG2SalidaAire() == null || d.tempG2EntradaAire() == null
            || d.tempG2CojAcoplado() == null || d.tempG2CojNoAcoplado() == null || d.tempG2CojEmpuje() == null
            || d.tempG2Aceite() == null || d.tempG2NucleoEstator() == null
            || d.tempG2EstatorFaseU() == null || d.tempG2EstatorFaseV() == null || d.tempG2EstatorFaseW() == null;
    }

    private void copyDtoToEntity(HourlyReadingCreateDTO d, HourlyReading r) {
        r.setObservations(d.observations());
        r.setNivelCarga(d.nivelCarga());
        r.setNivelDescarga(d.nivelDescarga());
        r.setServAuxKwh(d.servAuxKwh());
        r.setEpsaActarisKwh(d.epsaActarisKwh());
        r.setPotActivaG1(d.potActivaG1());
        r.setVoltExcG1(d.voltExcG1());
        r.setCorrExcG1(d.corrExcG1());
        r.setVoltG1rst(d.voltG1rst());
        r.setCorrG1faseR(d.corrG1faseR());
        r.setCorrG1faseS(d.corrG1faseS());
        r.setCorrG1faseT(d.corrG1faseT());
        r.setContActarisG1(d.contActarisG1());
        r.setTempTrafoF1(d.tempTrafoF1());
        r.setTempTrafoF2(d.tempTrafoF2());
        r.setTempTrafoF3(d.tempTrafoF3());
        r.setTempG1CojExc(d.tempG1CojExc());
        r.setTempG1SalidaAire(d.tempG1SalidaAire());
        r.setTempG1EntradaAire(d.tempG1EntradaAire());
        r.setTempG1CojAcoplado(d.tempG1CojAcoplado());
        r.setTempG1CojNoAcoplado(d.tempG1CojNoAcoplado());
        r.setTempG1CojEmpuje(d.tempG1CojEmpuje());
        r.setTempG1Aceite(d.tempG1Aceite());
        r.setTempG1SalidaAireExc(d.tempG1SalidaAireExc());
        r.setPotActivaG2(d.potActivaG2());
        r.setVoltExcG2(d.voltExcG2());
        r.setCorrExcG2(d.corrExcG2());
        r.setVoltG2rst(d.voltG2rst());
        r.setCorrG2faseR(d.corrG2faseR());
        r.setCorrG2faseS(d.corrG2faseS());
        r.setCorrG2faseT(d.corrG2faseT());
        r.setTempCojGuiaG2(d.tempCojGuiaG2());
        r.setTempCojAcopladoT2(d.tempCojAcopladoT2());
        r.setContActarisG2(d.contActarisG2());
        r.setTempG2CojExc(d.tempG2CojExc());
        r.setTempG2SalidaAire(d.tempG2SalidaAire());
        r.setTempG2EntradaAire(d.tempG2EntradaAire());
        r.setTempG2CojAcoplado(d.tempG2CojAcoplado());
        r.setTempG2CojNoAcoplado(d.tempG2CojNoAcoplado());
        r.setTempG2CojEmpuje(d.tempG2CojEmpuje());
        r.setTempG2Aceite(d.tempG2Aceite());
        r.setTempG2NucleoEstator(d.tempG2NucleoEstator());
        r.setTempG2EstatorFaseU(d.tempG2EstatorFaseU());
        r.setTempG2EstatorFaseV(d.tempG2EstatorFaseV());
        r.setTempG2EstatorFaseW(d.tempG2EstatorFaseW());
    }

    private void applyUpdateDtoToEntity(HourlyReadingUpdateDTO d, HourlyReading r) {
        if (d.observations() != null) r.setObservations(d.observations());
        if (d.nivelCarga() != null) r.setNivelCarga(d.nivelCarga());
        if (d.nivelDescarga() != null) r.setNivelDescarga(d.nivelDescarga());
        if (d.servAuxKwh() != null) r.setServAuxKwh(d.servAuxKwh());
        if (d.epsaActarisKwh() != null) r.setEpsaActarisKwh(d.epsaActarisKwh());
        if (d.potActivaG1() != null) r.setPotActivaG1(d.potActivaG1());
        if (d.voltExcG1() != null) r.setVoltExcG1(d.voltExcG1());
        if (d.corrExcG1() != null) r.setCorrExcG1(d.corrExcG1());
        if (d.voltG1rst() != null) r.setVoltG1rst(d.voltG1rst());
        if (d.corrG1faseR() != null) r.setCorrG1faseR(d.corrG1faseR());
        if (d.corrG1faseS() != null) r.setCorrG1faseS(d.corrG1faseS());
        if (d.corrG1faseT() != null) r.setCorrG1faseT(d.corrG1faseT());
        if (d.contActarisG1() != null) r.setContActarisG1(d.contActarisG1());
        if (d.tempTrafoF1() != null) r.setTempTrafoF1(d.tempTrafoF1());
        if (d.tempTrafoF2() != null) r.setTempTrafoF2(d.tempTrafoF2());
        if (d.tempTrafoF3() != null) r.setTempTrafoF3(d.tempTrafoF3());
        if (d.tempG1CojExc() != null) r.setTempG1CojExc(d.tempG1CojExc());
        if (d.tempG1SalidaAire() != null) r.setTempG1SalidaAire(d.tempG1SalidaAire());
        if (d.tempG1EntradaAire() != null) r.setTempG1EntradaAire(d.tempG1EntradaAire());
        if (d.tempG1CojAcoplado() != null) r.setTempG1CojAcoplado(d.tempG1CojAcoplado());
        if (d.tempG1CojNoAcoplado() != null) r.setTempG1CojNoAcoplado(d.tempG1CojNoAcoplado());
        if (d.tempG1CojEmpuje() != null) r.setTempG1CojEmpuje(d.tempG1CojEmpuje());
        if (d.tempG1Aceite() != null) r.setTempG1Aceite(d.tempG1Aceite());
        if (d.tempG1SalidaAireExc() != null) r.setTempG1SalidaAireExc(d.tempG1SalidaAireExc());
        if (d.potActivaG2() != null) r.setPotActivaG2(d.potActivaG2());
        if (d.voltExcG2() != null) r.setVoltExcG2(d.voltExcG2());
        if (d.corrExcG2() != null) r.setCorrExcG2(d.corrExcG2());
        if (d.voltG2rst() != null) r.setVoltG2rst(d.voltG2rst());
        if (d.corrG2faseR() != null) r.setCorrG2faseR(d.corrG2faseR());
        if (d.corrG2faseS() != null) r.setCorrG2faseS(d.corrG2faseS());
        if (d.corrG2faseT() != null) r.setCorrG2faseT(d.corrG2faseT());
        if (d.tempCojGuiaG2() != null) r.setTempCojGuiaG2(d.tempCojGuiaG2());
        if (d.tempCojAcopladoT2() != null) r.setTempCojAcopladoT2(d.tempCojAcopladoT2());
        if (d.contActarisG2() != null) r.setContActarisG2(d.contActarisG2());
        if (d.tempG2CojExc() != null) r.setTempG2CojExc(d.tempG2CojExc());
        if (d.tempG2SalidaAire() != null) r.setTempG2SalidaAire(d.tempG2SalidaAire());
        if (d.tempG2EntradaAire() != null) r.setTempG2EntradaAire(d.tempG2EntradaAire());
        if (d.tempG2CojAcoplado() != null) r.setTempG2CojAcoplado(d.tempG2CojAcoplado());
        if (d.tempG2CojNoAcoplado() != null) r.setTempG2CojNoAcoplado(d.tempG2CojNoAcoplado());
        if (d.tempG2CojEmpuje() != null) r.setTempG2CojEmpuje(d.tempG2CojEmpuje());
        if (d.tempG2Aceite() != null) r.setTempG2Aceite(d.tempG2Aceite());
        if (d.tempG2NucleoEstator() != null) r.setTempG2NucleoEstator(d.tempG2NucleoEstator());
        if (d.tempG2EstatorFaseU() != null) r.setTempG2EstatorFaseU(d.tempG2EstatorFaseU());
        if (d.tempG2EstatorFaseV() != null) r.setTempG2EstatorFaseV(d.tempG2EstatorFaseV());
        if (d.tempG2EstatorFaseW() != null) r.setTempG2EstatorFaseW(d.tempG2EstatorFaseW());
    }

    private void detectAndAuditChanges(HourlyReading r, HourlyReadingUpdateDTO d, User u, LocalDate date, int hour) {
        checkDiff(r, u, date, hour, "nivelCarga", "Niv. Carga (msnm)", r.getNivelCarga(), d.nivelCarga(), d.justification());
        checkDiff(r, u, date, hour, "nivelDescarga", "Niv. Descarga (msnm)", r.getNivelDescarga(), d.nivelDescarga(), d.justification());
        checkDiff(r, u, date, hour, "servAuxKwh", "Serv. Aux (kWh)", r.getServAuxKwh(), d.servAuxKwh(), d.justification());
        checkDiff(r, u, date, hour, "epsaActarisKwh", "EPSA Actaris (kWh)", r.getEpsaActarisKwh(), d.epsaActarisKwh(), d.justification());
        checkDiff(r, u, date, hour, "potActivaG1", "Pot. Activa G-1 (kW)", r.getPotActivaG1(), d.potActivaG1(), d.justification());
        checkDiff(r, u, date, hour, "voltExcG1", "V. Excit. G-1 (V)", r.getVoltExcG1(), d.voltExcG1(), d.justification());
        checkDiff(r, u, date, hour, "corrExcG1", "I. Excit. G-1 (A)", r.getCorrExcG1(), d.corrExcG1(), d.justification());
        checkDiff(r, u, date, hour, "voltG1rst", "V. RST G-1 (V)", r.getVoltG1rst(), d.voltG1rst(), d.justification());
        checkDiff(r, u, date, hour, "corrG1faseR", "I. Fase R G-1 (A)", r.getCorrG1faseR(), d.corrG1faseR(), d.justification());
        checkDiff(r, u, date, hour, "corrG1faseS", "I. Fase S G-1 (A)", r.getCorrG1faseS(), d.corrG1faseS(), d.justification());
        checkDiff(r, u, date, hour, "corrG1faseT", "I. Fase T G-1 (A)", r.getCorrG1faseT(), d.corrG1faseT(), d.justification());
        checkDiff(r, u, date, hour, "contActarisG1", "Cont. Actaris G-1 (kWh)", r.getContActarisG1(), d.contActarisG1(), d.justification());
        checkDiff(r, u, date, hour, "tempTrafoF1", "Trafo F1 (°C)", r.getTempTrafoF1(), d.tempTrafoF1(), d.justification());
        checkDiff(r, u, date, hour, "tempTrafoF2", "Trafo F2 (°C)", r.getTempTrafoF2(), d.tempTrafoF2(), d.justification());
        checkDiff(r, u, date, hour, "tempTrafoF3", "Trafo F3 (°C)", r.getTempTrafoF3(), d.tempTrafoF3(), d.justification());
        checkDiff(r, u, date, hour, "tempG1CojExc", "Coj. Excit. G-1 (°C)", r.getTempG1CojExc(), d.tempG1CojExc(), d.justification());
        checkDiff(r, u, date, hour, "tempG1SalidaAire", "Sal. Aire G-1 (°C)", r.getTempG1SalidaAire(), d.tempG1SalidaAire(), d.justification());
        checkDiff(r, u, date, hour, "tempG1EntradaAire", "Ent. Aire G-1 (°C)", r.getTempG1EntradaAire(), d.tempG1EntradaAire(), d.justification());
        checkDiff(r, u, date, hour, "tempG1CojAcoplado", "Coj. Acop. G-1 (°C)", r.getTempG1CojAcoplado(), d.tempG1CojAcoplado(), d.justification());
        checkDiff(r, u, date, hour, "tempG1CojNoAcoplado", "Coj. No Ac. G-1 (°C)", r.getTempG1CojNoAcoplado(), d.tempG1CojNoAcoplado(), d.justification());
        checkDiff(r, u, date, hour, "tempG1CojEmpuje", "Coj. Empuje G-1 (°C)", r.getTempG1CojEmpuje(), d.tempG1CojEmpuje(), d.justification());
        checkDiff(r, u, date, hour, "tempG1Aceite", "Aceite Cuba G-1 (°C)", r.getTempG1Aceite(), d.tempG1Aceite(), d.justification());
        checkDiff(r, u, date, hour, "tempG1SalidaAireExc", "Sal. Aire Exc. G-1 (°C)", r.getTempG1SalidaAireExc(), d.tempG1SalidaAireExc(), d.justification());
        checkDiff(r, u, date, hour, "potActivaG2", "Pot. Activa G-2 (kW)", r.getPotActivaG2(), d.potActivaG2(), d.justification());
        checkDiff(r, u, date, hour, "voltExcG2", "V. Excit. G-2 (V)", r.getVoltExcG2(), d.voltExcG2(), d.justification());
        checkDiff(r, u, date, hour, "corrExcG2", "I. Excit. G-2 (A)", r.getCorrExcG2(), d.corrExcG2(), d.justification());
        checkDiff(r, u, date, hour, "voltG2rst", "V. RST G-2 (V)", r.getVoltG2rst(), d.voltG2rst(), d.justification());
        checkDiff(r, u, date, hour, "corrG2faseR", "I. Fase R G-2 (A)", r.getCorrG2faseR(), d.corrG2faseR(), d.justification());
        checkDiff(r, u, date, hour, "corrG2faseS", "I. Fase S G-2 (A)", r.getCorrG2faseS(), d.corrG2faseS(), d.justification());
        checkDiff(r, u, date, hour, "corrG2faseT", "I. Fase T G-2 (A)", r.getCorrG2faseT(), d.corrG2faseT(), d.justification());
        checkDiff(r, u, date, hour, "tempCojGuiaG2", "Coj. Guía G2 (°C)", r.getTempCojGuiaG2(), d.tempCojGuiaG2(), d.justification());
        checkDiff(r, u, date, hour, "tempCojAcopladoT2", "Coj. Acop. T2 (°C)", r.getTempCojAcopladoT2(), d.tempCojAcopladoT2(), d.justification());
        checkDiff(r, u, date, hour, "contActarisG2", "Cont. Actaris G-2 (kWh)", r.getContActarisG2(), d.contActarisG2(), d.justification());
        checkDiff(r, u, date, hour, "tempG2CojExc", "Coj. Excit. G-2 (°C)", r.getTempG2CojExc(), d.tempG2CojExc(), d.justification());
        checkDiff(r, u, date, hour, "tempG2SalidaAire", "Sal. Aire G-2 (°C)", r.getTempG2SalidaAire(), d.tempG2SalidaAire(), d.justification());
        checkDiff(r, u, date, hour, "tempG2EntradaAire", "Ent. Aire G-2 (°C)", r.getTempG2EntradaAire(), d.tempG2EntradaAire(), d.justification());
        checkDiff(r, u, date, hour, "tempG2CojAcoplado", "Coj. Acop. G-2 (°C)", r.getTempG2CojAcoplado(), d.tempG2CojAcoplado(), d.justification());
        checkDiff(r, u, date, hour, "tempG2CojNoAcoplado", "Coj. No Ac. G-2 (°C)", r.getTempG2CojNoAcoplado(), d.tempG2CojNoAcoplado(), d.justification());
        checkDiff(r, u, date, hour, "tempG2CojEmpuje", "Coj. Empuje G-2 (°C)", r.getTempG2CojEmpuje(), d.tempG2CojEmpuje(), d.justification());
        checkDiff(r, u, date, hour, "tempG2Aceite", "Aceite Cuba G-2 (°C)", r.getTempG2Aceite(), d.tempG2Aceite(), d.justification());
        checkDiff(r, u, date, hour, "tempG2NucleoEstator", "Núcleo Est. G-2 (°C)", r.getTempG2NucleoEstator(), d.tempG2NucleoEstator(), d.justification());
        checkDiff(r, u, date, hour, "tempG2EstatorFaseU", "Est. Fase U G-2 (°C)", r.getTempG2EstatorFaseU(), d.tempG2EstatorFaseU(), d.justification());
        checkDiff(r, u, date, hour, "tempG2EstatorFaseV", "Est. Fase V G-2 (°C)", r.getTempG2EstatorFaseV(), d.tempG2EstatorFaseV(), d.justification());
        checkDiff(r, u, date, hour, "tempG2EstatorFaseW", "Est. Fase W G-2 (°C)", r.getTempG2EstatorFaseW(), d.tempG2EstatorFaseW(), d.justification());
    }

    private void validateUserInShift(User user) {
        if (user == null) {
            return;
        }
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return; // Supervisores y administradores conservan permisos de supervisión
        }
        User activeOperator = shiftService.getActiveOperator();
        if (activeOperator != null && !Objects.equals(user.getId(), activeOperator.getId())) {
            throw new BusinessRuleException("No está autorizado para registrar o modificar lecturas: el turno activo le pertenece a "
                + activeOperator.getFullName() + ". Solo el operador en turno puede registrar lecturas.");
        }
    }

    private void checkDiff(HourlyReading r, User u, LocalDate date, int hour,
                           String fieldKey, String fieldLabel, Double oldVal, Double newVal, String justification) {
        if (newVal != null && !Objects.equals(oldVal, newVal)) {
            String oldStr = oldVal != null ? HourlyReadingDTO.fmt(oldVal) : "—";
            String newStr = HourlyReadingDTO.fmt(newVal);
            auditService.recordAudit(r, u, date, hour, fieldKey, fieldLabel, oldStr, newStr, justification);
        }
    }

    public DailyReportDTO toDTO(DailyReport report) {
        List<HourlyReadingDTO> dtos = report.getReadings().stream()
            .map(this::toReadingDTO)
            .toList();

        int savedCount = 0;
        double totalGenBruta = 0;
        double totalG1 = 0;
        double totalG2 = 0;

        for (HourlyReading r : report.getReadings()) {
            if (Boolean.TRUE.equals(r.getSaved())) {
                savedCount++;
                if (r.getGenBrutaKwh() != null) totalGenBruta += r.getGenBrutaKwh();
                if (r.getKwhG1() != null) totalG1 += r.getKwhG1();
                if (r.getKwhG2() != null) totalG2 += r.getKwhG2();
            }
        }

        return new DailyReportDTO(
            report.getId(),
            report.getReportDate(),
            report.getStatus(),
            savedCount,
            totalGenBruta,
            totalG1,
            totalG2,
            totalG1 + totalG2,
            dtos
        );
    }

    public HourlyReadingDTO toReadingDTO(HourlyReading r) {
        return new HourlyReadingDTO(
            r.getHour(),
            Boolean.TRUE.equals(r.getSaved()),
            Boolean.TRUE.equals(r.getIsEdited()),
            r.getObservations() != null ? r.getObservations() : "",
            HourlyReadingDTO.fmt(r.getNivelCarga()),
            HourlyReadingDTO.fmt(r.getNivelDescarga()),
            HourlyReadingDTO.fmt(r.getServAuxKwh()),
            HourlyReadingDTO.fmt(r.getEpsaActarisKwh()),
            HourlyReadingDTO.fmt(r.getGenBrutaKwh()),
            HourlyReadingDTO.fmt(r.getPotActivaG1()),
            HourlyReadingDTO.fmt(r.getVoltExcG1()),
            HourlyReadingDTO.fmt(r.getCorrExcG1()),
            HourlyReadingDTO.fmt(r.getVoltG1rst()),
            HourlyReadingDTO.fmt(r.getCorrG1faseR()),
            HourlyReadingDTO.fmt(r.getCorrG1faseS()),
            HourlyReadingDTO.fmt(r.getCorrG1faseT()),
            HourlyReadingDTO.fmt(r.getContActarisG1()),
            HourlyReadingDTO.fmt(r.getKwhG1()),
            HourlyReadingDTO.fmt(r.getTempTrafoF1()),
            HourlyReadingDTO.fmt(r.getTempTrafoF2()),
            HourlyReadingDTO.fmt(r.getTempTrafoF3()),
            HourlyReadingDTO.fmt(r.getTempG1CojExc()),
            HourlyReadingDTO.fmt(r.getTempG1SalidaAire()),
            HourlyReadingDTO.fmt(r.getTempG1EntradaAire()),
            HourlyReadingDTO.fmt(r.getTempG1CojAcoplado()),
            HourlyReadingDTO.fmt(r.getTempG1CojNoAcoplado()),
            HourlyReadingDTO.fmt(r.getTempG1CojEmpuje()),
            HourlyReadingDTO.fmt(r.getTempG1Aceite()),
            HourlyReadingDTO.fmt(r.getTempG1SalidaAireExc()),
            HourlyReadingDTO.fmt(r.getPotActivaG2()),
            HourlyReadingDTO.fmt(r.getVoltExcG2()),
            HourlyReadingDTO.fmt(r.getCorrExcG2()),
            HourlyReadingDTO.fmt(r.getVoltG2rst()),
            HourlyReadingDTO.fmt(r.getCorrG2faseR()),
            HourlyReadingDTO.fmt(r.getCorrG2faseS()),
            HourlyReadingDTO.fmt(r.getCorrG2faseT()),
            HourlyReadingDTO.fmt(r.getTempCojGuiaG2()),
            HourlyReadingDTO.fmt(r.getTempCojAcopladoT2()),
            HourlyReadingDTO.fmt(r.getContActarisG2()),
            HourlyReadingDTO.fmt(r.getKwhG2()),
            HourlyReadingDTO.fmt(r.getTempG2CojExc()),
            HourlyReadingDTO.fmt(r.getTempG2SalidaAire()),
            HourlyReadingDTO.fmt(r.getTempG2EntradaAire()),
            HourlyReadingDTO.fmt(r.getTempG2CojAcoplado()),
            HourlyReadingDTO.fmt(r.getTempG2CojNoAcoplado()),
            HourlyReadingDTO.fmt(r.getTempG2CojEmpuje()),
            HourlyReadingDTO.fmt(r.getTempG2Aceite()),
            HourlyReadingDTO.fmt(r.getTempG2NucleoEstator()),
            HourlyReadingDTO.fmt(r.getTempG2EstatorFaseU()),
            HourlyReadingDTO.fmt(r.getTempG2EstatorFaseV()),
            HourlyReadingDTO.fmt(r.getTempG2EstatorFaseW())
        );
    }
}
