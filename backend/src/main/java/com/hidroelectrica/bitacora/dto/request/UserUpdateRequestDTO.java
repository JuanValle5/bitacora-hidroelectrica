package com.hidroelectrica.bitacora.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDTO(
    @NotBlank(message = "El nombre completo es obligatorio")
    String nombre,

    String rol,
    String turno,
    String password
) {}
