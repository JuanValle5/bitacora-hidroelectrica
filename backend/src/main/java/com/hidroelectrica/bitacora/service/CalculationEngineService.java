package com.hidroelectrica.bitacora.service;

import org.springframework.stereotype.Service;

@Service
public class CalculationEngineService {

    public static final double MULTIPLIER_GEN_BRUTA = 2400.0;
    public static final double MULTIPLIER_ACTARIS_G1 = 1363.63;
    public static final double MULTIPLIER_ACTARIS_G2 = 1363.63;

    public Double calculateGenBrutaKwh(Double currentEpsaActaris, Double previousEpsaActaris) {
        if (currentEpsaActaris == null || previousEpsaActaris == null) return null;
        double delta = currentEpsaActaris - previousEpsaActaris;
        if (delta < 0) return 0.0;
        return (double) Math.round(delta * MULTIPLIER_GEN_BRUTA);
    }

    public Double calculateKwhG1(Double currentContActarisG1, Double previousContActarisG1) {
        if (currentContActarisG1 == null || previousContActarisG1 == null) return null;
        double delta = currentContActarisG1 - previousContActarisG1;
        if (delta < 0) return 0.0;
        return (double) Math.round(delta * MULTIPLIER_ACTARIS_G1);
    }

    public Double calculateKwhG2(Double currentContActarisG2, Double previousContActarisG2) {
        if (currentContActarisG2 == null || previousContActarisG2 == null) return null;
        double delta = currentContActarisG2 - previousContActarisG2;
        if (delta < 0) return 0.0;
        return (double) Math.round(delta * MULTIPLIER_ACTARIS_G2);
    }
}
