package com.hidroelectrica.bitacora;

import com.hidroelectrica.bitacora.model.DailyReport;
import com.hidroelectrica.bitacora.model.HourlyReading;
import com.hidroelectrica.bitacora.model.ShiftHandoff;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.DailyReportRepository;
import com.hidroelectrica.bitacora.repository.ShiftHandoffRepository;
import com.hidroelectrica.bitacora.repository.UserRepository;
import com.hidroelectrica.bitacora.security.PasswordService;
import com.hidroelectrica.bitacora.service.CalculationEngineService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ShiftHandoffRepository shiftHandoffRepository;
    private final DailyReportRepository dailyReportRepository;
    private final CalculationEngineService calculationEngine;
    private final PasswordService passwordService;

    public DataInitializer(
        UserRepository userRepository,
        ShiftHandoffRepository shiftHandoffRepository,
        DailyReportRepository dailyReportRepository,
        CalculationEngineService calculationEngine,
        PasswordService passwordService
    ) {
        this.userRepository = userRepository;
        this.shiftHandoffRepository = shiftHandoffRepository;
        this.dailyReportRepository = dailyReportRepository;
        this.calculationEngine = calculationEngine;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        seedUsersIfEmpty();
        seedReportsIfEmpty();
    }

    private void seedUsersIfEmpty() {
        if (userRepository.count() == 0) {
            // Admin
            User admin = new User("admin", passwordService.hashPassword("admin123"), "Administrador Sistema", "ROLE_ADMIN", null);
            admin = userRepository.save(admin);

            // Operador principal (c.mendoza)
            User cMendoza = new User("c.mendoza", passwordService.hashPassword("op123"), "Carlos Mendoza", "ROLE_OPERATOR", "Turno A (06:00–18:00)");
            cMendoza = userRepository.save(cMendoza);

            // Sembrar relevo inicial hacia Carlos Mendoza
            if (shiftHandoffRepository.count() == 0) {
                ShiftHandoff initialHandoff = new ShiftHandoff(
                    LocalDate.now(),
                    6,
                    "Turno A",
                    admin,
                    cMendoza,
                    "Apertura inicial de bitácora y asignación de turno"
                );
                shiftHandoffRepository.save(initialHandoff);
            }
        }
    }

    private void seedReportsIfEmpty() {
        if (dailyReportRepository.count() == 0) {
            User operator = userRepository.findByUsername("c.mendoza")
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            // 1. Reporte de ayer con hora 23 guardada como línea base de contadores
            DailyReport repYesterday = new DailyReport(yesterday, operator);
            for (int h = 0; h < 24; h++) {
                HourlyReading r = new HourlyReading(h);
                if (h == 23) {
                    r.setSaved(true);
                    r.setLastModifiedBy(operator);
                    r.setNivelCarga(646.50);
                    r.setNivelDescarga(513.20);
                    r.setServAuxKwh(125840.00);
                    r.setEpsaActarisKwh(4823644.800);
                    r.setGenBrutaKwh(15000.0);
                    r.setPotActivaG1(5200.0);
                    r.setVoltExcG1(104.0);
                    r.setCorrExcG1(280.0);
                    r.setVoltG1rst(13800.0);
                    r.setCorrG1faseR(320.0);
                    r.setCorrG1faseS(320.0);
                    r.setCorrG1faseT(320.0);
                    r.setContActarisG1(1234562.300);
                    r.setKwhG1(6995.0);
                    r.setTempTrafoF1(52.0);
                    r.setTempTrafoF2(53.0);
                    r.setTempTrafoF3(51.5);
                    r.setTempG1CojExc(49.0);
                    r.setTempG1SalidaAire(44.0);
                    r.setTempG1EntradaAire(36.0);
                    r.setTempG1CojAcoplado(48.0);
                    r.setTempG1CojNoAcoplado(46.0);
                    r.setTempG1CojEmpuje(49.0);
                    r.setTempG1Aceite(51.0);
                    r.setTempG1SalidaAireExc(42.0);
                    r.setPotActivaG2(4900.0);
                    r.setVoltExcG2(101.0);
                    r.setCorrExcG2(265.0);
                    r.setVoltG2rst(13800.0);
                    r.setCorrG2faseR(305.0);
                    r.setCorrG2faseS(305.0);
                    r.setCorrG2faseT(305.0);
                    r.setTempCojGuiaG2(47.0);
                    r.setTempCojAcopladoT2(46.0);
                    r.setContActarisG2(987649.400);
                    r.setKwhG2(6545.0);
                    r.setTempG2CojExc(48.0);
                    r.setTempG2SalidaAire(43.0);
                    r.setTempG2EntradaAire(35.0);
                    r.setTempG2CojAcoplado(47.0);
                    r.setTempG2CojNoAcoplado(45.0);
                    r.setTempG2CojEmpuje(48.0);
                    r.setTempG2Aceite(50.0);
                    r.setTempG2NucleoEstator(68.0);
                    r.setTempG2EstatorFaseU(70.0);
                    r.setTempG2EstatorFaseV(69.0);
                    r.setTempG2EstatorFaseW(71.0);
                }
                repYesterday.addReading(r);
            }
            dailyReportRepository.save(repYesterday);

            // 2. Reporte de hoy con horas 0 a 13 registradas y calculadas
            DailyReport repToday = new DailyReport(today, operator);
            double baseEpsa = 4823644.800;
            double baseG1 = 1234562.300;
            double baseG2 = 987649.400;
            double servAux = 125840.00;

            for (int h = 0; h < 24; h++) {
                HourlyReading r = new HourlyReading(h);
                if (h <= 13) {
                    double prevEpsa = baseEpsa + h * 6.25;
                    double curEpsa = Math.round((baseEpsa + (h + 1) * 6.25) * 1000.0) / 1000.0;
                    double prevG1 = baseG1 + h * 5.13;
                    double curG1 = Math.round((baseG1 + (h + 1) * 5.13) * 1000.0) / 1000.0;
                    double prevG2 = baseG2 + h * 4.80;
                    double curG2 = Math.round((baseG2 + (h + 1) * 4.80) * 1000.0) / 1000.0;
                    servAux += 35.0;

                    r.setSaved(true);
                    r.setLastModifiedBy(operator);
                    r.setNivelCarga(646.00 + (h % 3) * 0.4);
                    r.setNivelDescarga(513.00 + (h % 2) * 0.2);
                    r.setServAuxKwh(servAux);
                    r.setEpsaActarisKwh(curEpsa);
                    r.setGenBrutaKwh(calculationEngine.calculateGenBrutaKwh(curEpsa, prevEpsa));

                    r.setPotActivaG1(5200.0 + (h % 4) * 50.0);
                    r.setVoltExcG1(104.0);
                    r.setCorrExcG1(280.0);
                    r.setVoltG1rst(13800.0);
                    r.setCorrG1faseR(320.0);
                    r.setCorrG1faseS(320.0);
                    r.setCorrG1faseT(320.0);
                    r.setContActarisG1(curG1);
                    r.setKwhG1(calculationEngine.calculateKwhG1(curG1, prevG1));

                    r.setTempTrafoF1(52.0 + (h % 3));
                    r.setTempTrafoF2(53.0 + (h % 3));
                    r.setTempTrafoF3(51.5 + (h % 3));

                    r.setTempG1CojExc(49.0);
                    r.setTempG1SalidaAire(44.0);
                    r.setTempG1EntradaAire(36.0);
                    r.setTempG1CojAcoplado(48.0);
                    r.setTempG1CojNoAcoplado(46.0);
                    r.setTempG1CojEmpuje(49.0);
                    r.setTempG1Aceite(51.0);
                    r.setTempG1SalidaAireExc(42.0);

                    r.setPotActivaG2(4900.0 + (h % 4) * 45.0);
                    r.setVoltExcG2(101.0);
                    r.setCorrExcG2(265.0);
                    r.setVoltG2rst(13800.0);
                    r.setCorrG2faseR(305.0);
                    r.setCorrG2faseS(305.0);
                    r.setCorrG2faseT(305.0);
                    r.setTempCojGuiaG2(47.0);
                    r.setTempCojAcopladoT2(46.0);
                    r.setContActarisG2(curG2);
                    r.setKwhG2(calculationEngine.calculateKwhG2(curG2, prevG2));

                    r.setTempG2CojExc(48.0);
                    r.setTempG2SalidaAire(43.0);
                    r.setTempG2EntradaAire(35.0);
                    r.setTempG2CojAcoplado(47.0);
                    r.setTempG2CojNoAcoplado(45.0);
                    r.setTempG2CojEmpuje(48.0);
                    r.setTempG2Aceite(50.0);
                    r.setTempG2NucleoEstator(68.0);
                    r.setTempG2EstatorFaseU(70.0);
                    r.setTempG2EstatorFaseV(69.0);
                    r.setTempG2EstatorFaseW(71.0);
                }
                repToday.addReading(r);
            }
            dailyReportRepository.save(repToday);
        }
    }
}
