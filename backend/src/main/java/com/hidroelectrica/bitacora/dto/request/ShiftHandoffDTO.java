package com.hidroelectrica.bitacora.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ShiftHandoffDTO(
    @NotNull(message = "La fecha es obligatoria")
    LocalDate date,

    @NotNull(message = "La hora es obligatoria")
    Integer hour,

    String shiftType, // e.g. "Turno A", "Turno B"

    @NotNull(message = "El usuario receptor es obligatorio")
    Long receivingUserId,

    String notes
) {}
