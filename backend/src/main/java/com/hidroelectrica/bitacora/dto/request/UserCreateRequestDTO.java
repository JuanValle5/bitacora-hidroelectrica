package com.hidroelectrica.bitacora.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDTO(
    @NotBlank(message = "El nombre completo es obligatorio")
    String nombre,

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    String usuario,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password,

    String rol,   // "admin" o "operador"
    String turno  // Opcional, e.g. "Turno A (06:00–18:00)"
) {}
