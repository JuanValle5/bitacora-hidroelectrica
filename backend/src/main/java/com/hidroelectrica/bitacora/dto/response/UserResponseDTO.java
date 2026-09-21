package com.hidroelectrica.bitacora.dto.response;

public record UserResponseDTO(
    Long id,
    String nombre,
    String usuario,
    String rol,       // "admin" o "operador"
    boolean activo,
    String turno
) {}
