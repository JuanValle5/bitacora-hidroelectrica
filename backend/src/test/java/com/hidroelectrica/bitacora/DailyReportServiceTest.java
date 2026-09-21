package com.hidroelectrica.bitacora;

import com.hidroelectrica.bitacora.dto.request.HourlyReadingCreateDTO;
import com.hidroelectrica.bitacora.dto.request.HourlyReadingUpdateDTO;
import com.hidroelectrica.bitacora.dto.response.DailyReportDTO;
import com.hidroelectrica.bitacora.dto.response.HourlyReadingDTO;
import com.hidroelectrica.bitacora.exception.BusinessRuleException;
import com.hidroelectrica.bitacora.model.DailyReport;
import com.hidroelectrica.bitacora.model.HourlyReading;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.DailyReportRepository;
import com.hidroelectrica.bitacora.repository.HourlyReadingRepository;
import com.hidroelectrica.bitacora.service.AuditService;
import com.hidroelectrica.bitacora.service.CalculationEngineService;
import com.hidroelectrica.bitacora.service.DailyReportService;
import com.hidroelectrica.bitacora.service.ShiftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

    @Mock
    private DailyReportRepository reportRepository;

    @Mock
    private HourlyReadingRepository readingRepository;

    @Spy
    private CalculationEngineService calculationEngine = new CalculationEngineService();

    @Mock
    private AuditService auditService;

    @Mock
    private ShiftService shiftService;

    @InjectMocks
    private DailyReportService dailyReportService;

    private User sampleUser;
    private DailyReport sampleReport;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        sampleUser = new User("c.mendoza", "hash", "Carlos Mendoza", "ROLE_OPERATOR", "Turno A");
        sampleUser.setId(1L);

        lenient().when(shiftService.getActiveOperator()).thenReturn(sampleUser);

        testDate = LocalDate.of(2026, 9, 20);
        sampleReport = new DailyReport(testDate, sampleUser);
        sampleReport.setId(10L);

        for (int h = 0; h < 24; h++) {
            HourlyReading r = new HourlyReading(h);
            r.setId((long) (h + 1));
            sampleReport.addReading(r);
        }
    }

    @Test
    @DisplayName("Debe inicializar las 24 horas si el reporte diario es nuevo")
    void testGetOrCreateReport() {
        when(reportRepository.findByReportDate(testDate)).thenReturn(Optional.of(sampleReport));

        DailyReportDTO dto = dailyReportService.getReportByDate(testDate);

        assertNotNull(dto);
        assertEquals(24, dto.readings().size());
        assertEquals(0, dto.savedHoursCount());
    }

    @Test
    @DisplayName("RF-3: Debe rechazar lectura con campos vacíos y sin observaciones")
    void testRegisterReadingMissingDataWithoutObservations() {
        HourlyReadingCreateDTO dto = new HourlyReadingCreateDTO(
            14, "", null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null
        );

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
            dailyReportService.registerReading(testDate, dto, sampleUser)
        );

        assertTrue(ex.getMessage().contains("observaciones"));
    }

    @Test
    @DisplayName("RF-3: Debe permitir lectura con campos vacíos si se especifican observaciones")
    void testRegisterReadingMissingDataWithObservations() {
        HourlyReadingCreateDTO dto = new HourlyReadingCreateDTO(
            14, "Sensor limnímetro fuera de servicio por mantenimiento preventivo",
            null, 513.10, 125890.0, 4823660.0,
            6200.0, 105.0, 280.0, 13800.0, 310.0, 310.0, 310.0,
            1234575.0, 54.0, 54.0, 54.0, 50.0, 44.0, 38.0,
            48.0, 46.0, 49.0, 51.0, 42.0, 5900.0, 102.0, 270.0,
            13800.0, 295.0, 295.0, 295.0, 47.0, 46.0, 987660.0,
            49.0, 43.0, 37.0, 47.0, 45.0, 48.0, 50.0, 71.0, 73.0, 72.0, 74.0
        );

        HourlyReading target = sampleReport.getReadings().get(14);
        when(reportRepository.findByReportDate(testDate)).thenReturn(Optional.of(sampleReport));
        when(readingRepository.findByDailyReportIdAndHour(10L, 14)).thenReturn(Optional.of(target));
        when(readingRepository.save(any(HourlyReading.class))).thenAnswer(i -> i.getArgument(0));

        HourlyReadingDTO saved = dailyReportService.registerReading(testDate, dto, sampleUser);

        assertNotNull(saved);
        assertTrue(saved.saved());
        assertEquals("Sensor limnímetro fuera de servicio por mantenimiento preventivo", saved.observations());
    }

    @Test
    @DisplayName("RF-4: Debe rechazar actualización con justificación menor a 10 caracteres")
    void testUpdateReadingShortJustification() {
        HourlyReadingUpdateDTO dto = new HourlyReadingUpdateDTO(
            "error", null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null
        );

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
            dailyReportService.updateReading(testDate, 14, dto, sampleUser)
        );

        assertTrue(ex.getMessage().contains("al menos 10 caracteres"));
    }

    @Test
    @DisplayName("RF-4: Modificación de lectura debe crear auditoría y marcar isEdited = true")
    void testUpdateReadingAuditsDifferences() {
        HourlyReading reading = sampleReport.getReadings().get(14);
        reading.setSaved(true);
        reading.setPotActivaG1(5480.0);

        HourlyReadingUpdateDTO dto = new HourlyReadingUpdateDTO(
            "Error de transcripción en lectura inicial. Corregido según panel.",
            "Ajuste verificado", null, null, null, null,
            5720.0, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null
        );

        when(reportRepository.findByReportDate(testDate)).thenReturn(Optional.of(sampleReport));
        when(readingRepository.findByDailyReportIdAndHour(10L, 14)).thenReturn(Optional.of(reading));
        when(readingRepository.save(any(HourlyReading.class))).thenAnswer(i -> i.getArgument(0));

        HourlyReadingDTO updated = dailyReportService.updateReading(testDate, 14, dto, sampleUser);

        assertNotNull(updated);
        assertTrue(updated.isEdited());
        assertEquals("5720", updated.potActivaG1());

        verify(auditService, times(1)).recordAudit(
            eq(reading), eq(sampleUser), eq(testDate), eq(14),
            eq("potActivaG1"), eq("Pot. Activa G-1 (kW)"), eq("5480"), eq("5720"),
            eq("Error de transcripción en lectura inicial. Corregido según panel.")
        );
    }

    @Test
    @DisplayName("RF-5: Debe rechazar registro si el usuario no es el operador en turno")
    void testRegisterReading_UserNotOnDuty_ThrowsException() {
        User otherOperator = new User("r.torres", "hash", "Ramiro Torres", "ROLE_OPERATOR", "Turno B");
        otherOperator.setId(2L);

        when(shiftService.getActiveOperator()).thenReturn(sampleUser); // sampleUser tiene ID 1

        HourlyReadingCreateDTO dto = new HourlyReadingCreateDTO(
            10, "Campos incompletos por mantenimiento",
            null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null
        );

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
            dailyReportService.registerReading(testDate, dto, otherOperator)
        );

        assertTrue(ex.getMessage().contains("No está autorizado para registrar o modificar lecturas"));
        assertTrue(ex.getMessage().contains("Carlos Mendoza"));
    }

    @Test
    @DisplayName("RF-5: Administrador puede registrar lectura aun si no es el operador en turno")
    void testRegisterReading_AdminUser_Allowed() {
        User adminUser = new User("admin", "hash", "Administrador", "ROLE_ADMIN", null);
        adminUser.setId(99L);

        when(reportRepository.findByReportDate(testDate)).thenReturn(Optional.of(sampleReport));
        when(readingRepository.findByDailyReportIdAndHour(10L, 10))
            .thenReturn(Optional.of(sampleReport.getReadings().get(10)));
        when(readingRepository.save(any(HourlyReading.class))).thenAnswer(i -> i.getArgument(0));

        HourlyReadingCreateDTO dto = new HourlyReadingCreateDTO(
            10, "Supervisión administrativa",
            null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null
        );

        HourlyReadingDTO result = dailyReportService.registerReading(testDate, dto, adminUser);
        assertNotNull(result);
        assertEquals(10, result.hour());
    }

    @Test
    @DisplayName("Cálculo: Debe calcular genBrutaKwh, kwhG1 y kwhG2 con base en la hora previa")
    void testRegisterReading_CalculatesDerivedFieldsFromPreviousHour() {
        HourlyReading r13 = sampleReport.getReadings().get(13);
        r13.setSaved(true);
        r13.setEpsaActarisKwh(4823644.800);
        r13.setContActarisG1(1234562.300);
        r13.setContActarisG2(987649.400);

        HourlyReading r14 = sampleReport.getReadings().get(14);

        when(reportRepository.findByReportDate(testDate)).thenReturn(Optional.of(sampleReport));
        when(readingRepository.findByDailyReportIdAndHour(10L, 14)).thenReturn(Optional.of(r14));
        when(readingRepository.save(any(HourlyReading.class))).thenAnswer(i -> i.getArgument(0));

        HourlyReadingCreateDTO dto = new HourlyReadingCreateDTO(
            14, "Operación normal",
            646.5, 513.2, 125890.0, 4823651.050, // delta = 6.250 -> * 2400 = 15000
            5200.0, 105.0, 280.0, 13800.0, 310.0, 310.0, 310.0,
            1234567.430, // delta = 5.130 -> * 1363.63 = 6995
            54.0, 54.0, 54.0, 50.0, 44.0, 38.0,
            48.0, 46.0, 49.0, 51.0, 42.0, 4900.0, 102.0, 270.0,
            13800.0, 295.0, 295.0, 295.0, 47.0, 46.0,
            987654.200, // delta = 4.800 -> * 1363.63 = 6545
            49.0, 43.0, 37.0, 47.0, 45.0, 48.0, 50.0, 71.0, 73.0, 72.0, 74.0
        );

        HourlyReadingDTO saved = dailyReportService.registerReading(testDate, dto, sampleUser);

        assertNotNull(saved);
        assertEquals("15000", saved.genBrutaKwh());
        assertEquals("6995", saved.kwhG1());
        assertEquals("6545", saved.kwhG2());
    }

    @Test
    @DisplayName("Cálculo: Hora 0 debe calcular derivadas usando la hora 23 del día anterior")
    void testRegisterReading_Hour0_UsesYesterdayHour23() {
        LocalDate yesterday = testDate.minusDays(1);
        DailyReport yesterdayReport = new DailyReport(yesterday, sampleUser);
        yesterdayReport.setId(9L);
        HourlyReading y23 = new HourlyReading(23);
        y23.setSaved(true);
        y23.setEpsaActarisKwh(4823644.800);
        y23.setContActarisG1(1234562.300);
        y23.setContActarisG2(987649.400);
        yesterdayReport.addReading(y23);

        HourlyReading r0 = sampleReport.getReadings().get(0);

        when(reportRepository.findByReportDate(testDate)).thenReturn(Optional.of(sampleReport));
        when(reportRepository.findByReportDate(yesterday)).thenReturn(Optional.of(yesterdayReport));
        when(readingRepository.findByDailyReportIdAndHour(10L, 0)).thenReturn(Optional.of(r0));
        when(readingRepository.save(any(HourlyReading.class))).thenAnswer(i -> i.getArgument(0));

        HourlyReadingCreateDTO dto = new HourlyReadingCreateDTO(
            0, "Inicio de jornada",
            646.5, 513.2, 125840.0, 4823651.050, // delta = 6.250 -> * 2400 = 15000
            5200.0, 105.0, 280.0, 13800.0, 310.0, 310.0, 310.0,
            1234567.430, // delta = 5.130 -> * 1363.63 = 6995
            54.0, 54.0, 54.0, 50.0, 44.0, 38.0,
            48.0, 46.0, 49.0, 51.0, 42.0, 4900.0, 102.0, 270.0,
            13800.0, 295.0, 295.0, 295.0, 47.0, 46.0,
            987654.200, // delta = 4.800 -> * 1363.63 = 6545
            49.0, 43.0, 37.0, 47.0, 45.0, 48.0, 50.0, 71.0, 73.0, 72.0, 74.0
        );

        HourlyReadingDTO saved = dailyReportService.registerReading(testDate, dto, sampleUser);

        assertNotNull(saved);
        assertEquals("15000", saved.genBrutaKwh());
        assertEquals("6995", saved.kwhG1());
        assertEquals("6545", saved.kwhG2());
    }
}
