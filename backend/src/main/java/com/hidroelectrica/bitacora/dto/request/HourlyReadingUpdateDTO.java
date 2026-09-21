package com.hidroelectrica.bitacora.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HourlyReadingUpdateDTO(
    @NotBlank(message = "La justificación del cambio es obligatoria")
    @Size(min = 10, message = "La justificación debe tener al menos 10 caracteres")
    String justification,

    String observations,

    // B, C: Hidráulico
    Double nivelCarga,
    Double nivelDescarga,

    // D, E: Medición Frontera
    Double servAuxKwh,
    Double epsaActarisKwh,

    // G..M: G-1 Eléctrico
    Double potActivaG1,
    Double voltExcG1,
    Double corrExcG1,
    Double voltG1rst,
    Double corrG1faseR,
    Double corrG1faseS,
    Double corrG1faseT,

    // N: Medición G-1
    Double contActarisG1,

    // P..R: Transformador 1.500 KVA
    Double tempTrafoF1,
    Double tempTrafoF2,
    Double tempTrafoF3,

    // S..Z: Temperaturas G-1
    Double tempG1CojExc,
    Double tempG1SalidaAire,
    Double tempG1EntradaAire,
    Double tempG1CojAcoplado,
    Double tempG1CojNoAcoplado,
    Double tempG1CojEmpuje,
    Double tempG1Aceite,
    Double tempG1SalidaAireExc,

    // AC..AI: G-2 Eléctrico
    Double potActivaG2,
    Double voltExcG2,
    Double corrExcG2,
    Double voltG2rst,
    Double corrG2faseR,
    Double corrG2faseS,
    Double corrG2faseT,

    // AJ, AK: Mecánico G-2
    Double tempCojGuiaG2,
    Double tempCojAcopladoT2,

    // AL: Medición G-2
    Double contActarisG2,

    // AN..AT: Temperaturas G-2
    Double tempG2CojExc,
    Double tempG2SalidaAire,
    Double tempG2EntradaAire,
    Double tempG2CojAcoplado,
    Double tempG2CojNoAcoplado,
    Double tempG2CojEmpuje,
    Double tempG2Aceite,

    // AU..AX: Estator G-2
    Double tempG2NucleoEstator,
    Double tempG2EstatorFaseU,
    Double tempG2EstatorFaseV,
    Double tempG2EstatorFaseW
) {}
