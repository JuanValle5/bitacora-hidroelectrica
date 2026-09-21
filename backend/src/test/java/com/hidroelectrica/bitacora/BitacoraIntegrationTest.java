package com.hidroelectrica.bitacora;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BitacoraIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("IT: Login con usuario admin debe retornar 200 OK y token JWT")
    void testLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "admin",
                        "password": "admin123"
                    }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.usuario").value("admin"))
            .andExpect(jsonPath("$.user.rol").value("admin"));
    }

    @Test
    @DisplayName("IT: Consultar reporte diario debe devolver estructura de 24 horas")
    void testGetDailyReport() throws Exception {
        mockMvc.perform(get("/api/v1/daily-reports")
                .param("date", "2026-09-20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-09-20"))
            .andExpect(jsonPath("$.readings.length()").value(24));
    }

    @Test
    @DisplayName("IT: Métricas del dashboard deben retornar turno activo y estado")
    void testGetDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/metrics")
                .param("date", "2026-09-20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalHours").value(24))
            .andExpect(jsonPath("$.activeShiftLabel").isNotEmpty());
    }

    @Test
    @DisplayName("IT: Exportación de PDF debe retornar 200 y Content-Type application/pdf")
    void testExportPdf() throws Exception {
        // Asegurar que exista el reporte
        mockMvc.perform(get("/api/v1/daily-reports")
                .param("date", "2026-09-20"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/daily-reports/2026-09-20/export/pdf"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"bitacora-hidroelectrica-2026-09-20.pdf\""));
    }

    @Test
    @DisplayName("IT: Listar usuarios debe retornar la lista de usuarios del sistema")
    void testListUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
