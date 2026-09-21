package com.hidroelectrica.bitacora.dto.response;

public record AuditLogResponseDTO(
    Long id,
    String timestamp,
    String operador,
    String hora,
    String campo,
    String valorAnterior,
    String valorNuevo,
    String justificacion
) {}
