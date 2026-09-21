package com.hidroelectrica.bitacora.dto.response;

public record HourlyReadingDTO(
    int hour,
    boolean saved,
    boolean isEdited,
    String observations,

    // B, C: Hidráulico
    String nivelCarga,
    String nivelDescarga,

    // D, E, F: Medición Frontera
    String servAuxKwh,
    String epsaActarisKwh,
    String genBrutaKwh,

    // G..M: G-1 Eléctrico
    String potActivaG1,
    String voltExcG1,
    String corrExcG1,
    String voltG1rst,
    String corrG1faseR,
    String corrG1faseS,
    String corrG1faseT,

    // N, O: Medición G-1
    String contActarisG1,
    String kwhG1,

    // P..R: Transformador 1.500 KVA
    String tempTrafoF1,
    String tempTrafoF2,
    String tempTrafoF3,

    // S..Z: Temperaturas G-1
    String tempG1CojExc,
    String tempG1SalidaAire,
    String tempG1EntradaAire,
    String tempG1CojAcoplado,
    String tempG1CojNoAcoplado,
    String tempG1CojEmpuje,
    String tempG1Aceite,
    String tempG1SalidaAireExc,

    // AC..AI: G-2 Eléctrico
    String potActivaG2,
    String voltExcG2,
    String corrExcG2,
    String voltG2rst,
    String corrG2faseR,
    String corrG2faseS,
    String corrG2faseT,

    // AJ, AK: Mecánico G-2
    String tempCojGuiaG2,
    String tempCojAcopladoT2,

    // AL, AM: Medición G-2
    String contActarisG2,
    String kwhG2,

    // AN..AT: Temperaturas G-2
    String tempG2CojExc,
    String tempG2SalidaAire,
    String tempG2EntradaAire,
    String tempG2CojAcoplado,
    String tempG2CojNoAcoplado,
    String tempG2CojEmpuje,
    String tempG2Aceite,

    // AU..AX: Estator G-2
    String tempG2NucleoEstator,
    String tempG2EstatorFaseU,
    String tempG2EstatorFaseV,
    String tempG2EstatorFaseW
) {
    public static String fmt(Double val) {
        if (val == null) return "";
        if (val == Math.floor(val) && !Double.isInfinite(val)) {
            return String.valueOf(val.longValue());
        }
        return String.valueOf(val);
    }
}
