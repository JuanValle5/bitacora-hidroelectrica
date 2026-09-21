package com.hidroelectrica.bitacora;

import com.hidroelectrica.bitacora.service.CalculationEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculationEngineServiceTest {

    private CalculationEngineService calculationEngine;

    @BeforeEach
    void setUp() {
        calculationEngine = new CalculationEngineService();
    }

    @Test
    @DisplayName("Debe calcular la generación bruta multiplicando el delta por 2400")
    void testCalculateGenBrutaKwh() {
        Double prev = 4823651.000;
        Double cur = 4823658.200; // Delta: 7.200
        Double expected = (double) Math.round(7.200 * 2400.0); // 17280.0

        Double result = calculationEngine.calculateGenBrutaKwh(cur, prev);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Debe calcular kWh G1 multiplicando el delta de contador por 1363.63")
    void testCalculateKwhG1() {
        Double prev = 1234567.400;
        Double cur = 1234572.500; // Delta: 5.100
        Double expected = (double) Math.round(5.100 * 1363.63); // 6955.0

        Double result = calculationEngine.calculateKwhG1(cur, prev);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Debe calcular kWh G2 multiplicando el delta de contador por 1363.63")
    void testCalculateKwhG2() {
        Double prev = 987654.200;
        Double cur = 987658.900; // Delta: 4.700
        Double expected = (double) Math.round(4.700 * 1363.63); // 6409.0

        Double result = calculationEngine.calculateKwhG2(cur, prev);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Debe retornar null si algún valor es nulo")
    void testNullValues() {
        assertNull(calculationEngine.calculateGenBrutaKwh(null, 100.0));
        assertNull(calculationEngine.calculateGenBrutaKwh(100.0, null));
        assertNull(calculationEngine.calculateKwhG1(null, 100.0));
        assertNull(calculationEngine.calculateKwhG2(100.0, null));
    }

    @Test
    @DisplayName("Debe retornar 0.0 si el delta es negativo (ej. reseteo anómalo)")
    void testNegativeDelta() {
        assertEquals(0.0, calculationEngine.calculateGenBrutaKwh(100.0, 110.0));
        assertEquals(0.0, calculationEngine.calculateKwhG1(100.0, 110.0));
        assertEquals(0.0, calculationEngine.calculateKwhG2(100.0, 110.0));
    }
}
