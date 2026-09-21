package com.hidroelectrica.bitacora.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_readings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"daily_report_id", "hour"})
})
public class HourlyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    @JsonIgnore
    private DailyReport dailyReport;

    @Column(nullable = false)
    private Integer hour; // 0 to 23

    @Column(nullable = false)
    private Boolean saved = false;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;

    @Column(columnDefinition = "TEXT")
    private String observations;

    // ── B, C: Hidráulico ───────────────────────────────────────────────────
    @Column(name = "nivel_carga")
    private Double nivelCarga;

    @Column(name = "nivel_descarga")
    private Double nivelDescarga;

    // ── D, E, F: Medición Frontera ──────────────────────────────────────────
    @Column(name = "serv_aux_kwh")
    private Double servAuxKwh;

    @Column(name = "epsa_actaris_kwh")
    private Double epsaActarisKwh;

    @Column(name = "gen_bruta_kwh")
    private Double genBrutaKwh; // Calculado: (E_t - E_t-1) * 2400

    // ── G..M: G-1 Eléctrico ────────────────────────────────────────────────
    @Column(name = "pot_activa_g1")
    private Double potActivaG1;

    @Column(name = "volt_exc_g1")
    private Double voltExcG1;

    @Column(name = "corr_exc_g1")
    private Double corrExcG1;

    @Column(name = "volt_g1_rst")
    private Double voltG1rst;

    @Column(name = "corr_g1_fase_r")
    private Double corrG1faseR;

    @Column(name = "corr_g1_fase_s")
    private Double corrG1faseS;

    @Column(name = "corr_g1_fase_t")
    private Double corrG1faseT;

    // ── N, O: Medición G-1 ─────────────────────────────────────────────────
    @Column(name = "cont_actaris_g1")
    private Double contActarisG1;

    @Column(name = "kwh_g1")
    private Double kwhG1; // Calculado: (N_t - N_t-1) * 1363.63

    // ── P..R: Transformador 1.500 KVA ──────────────────────────────────────
    @Column(name = "temp_trafo_f1")
    private Double tempTrafoF1;

    @Column(name = "temp_trafo_f2")
    private Double tempTrafoF2;

    @Column(name = "temp_trafo_f3")
    private Double tempTrafoF3;

    // ── S..Z: Temperaturas G-1 ─────────────────────────────────────────────
    @Column(name = "temp_g1_coj_exc")
    private Double tempG1CojExc;

    @Column(name = "temp_g1_salida_aire")
    private Double tempG1SalidaAire;

    @Column(name = "temp_g1_entrada_aire")
    private Double tempG1EntradaAire;

    @Column(name = "temp_g1_coj_acoplado")
    private Double tempG1CojAcoplado;

    @Column(name = "temp_g1_coj_no_acoplado")
    private Double tempG1CojNoAcoplado;

    @Column(name = "temp_g1_coj_empuje")
    private Double tempG1CojEmpuje;

    @Column(name = "temp_g1_aceite")
    private Double tempG1Aceite;

    @Column(name = "temp_g1_salida_aire_exc")
    private Double tempG1SalidaAireExc;

    // ── AC..AI: G-2 Eléctrico ──────────────────────────────────────────────
    @Column(name = "pot_activa_g2")
    private Double potActivaG2;

    @Column(name = "volt_exc_g2")
    private Double voltExcG2;

    @Column(name = "corr_exc_g2")
    private Double corrExcG2;

    @Column(name = "volt_g2_rst")
    private Double voltG2rst;

    @Column(name = "corr_g2_fase_r")
    private Double corrG2faseR;

    @Column(name = "corr_g2_fase_s")
    private Double corrG2faseS;

    @Column(name = "corr_g2_fase_t")
    private Double corrG2faseT;

    // ── AJ, AK: Mecánico G-2 ───────────────────────────────────────────────
    @Column(name = "temp_coj_guia_g2")
    private Double tempCojGuiaG2;

    @Column(name = "temp_coj_acoplado_t2")
    private Double tempCojAcopladoT2;

    // ── AL, AM: Medición G-2 ───────────────────────────────────────────────
    @Column(name = "cont_actaris_g2")
    private Double contActarisG2;

    @Column(name = "kwh_g2")
    private Double kwhG2; // Calculado: (AL_t - AL_t-1) * 1363.63

    // ── AN..AT: Temperaturas G-2 ───────────────────────────────────────────
    @Column(name = "temp_g2_coj_exc")
    private Double tempG2CojExc;

    @Column(name = "temp_g2_salida_aire")
    private Double tempG2SalidaAire;

    @Column(name = "temp_g2_entrada_aire")
    private Double tempG2EntradaAire;

    @Column(name = "temp_g2_coj_acoplado")
    private Double tempG2CojAcoplado;

    @Column(name = "temp_g2_coj_no_acoplado")
    private Double tempG2CojNoAcoplado;

    @Column(name = "temp_g2_coj_empuje")
    private Double tempG2CojEmpuje;

    @Column(name = "temp_g2_aceite")
    private Double tempG2Aceite;

    // ── AU..AX: Estator G-2 ────────────────────────────────────────────────
    @Column(name = "temp_g2_nucleo_estator")
    private Double tempG2NucleoEstator;

    @Column(name = "temp_g2_estator_fase_u")
    private Double tempG2EstatorFaseU;

    @Column(name = "temp_g2_estator_fase_v")
    private Double tempG2EstatorFaseV;

    @Column(name = "temp_g2_estator_fase_w")
    private Double tempG2EstatorFaseW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public HourlyReading() {}

    public HourlyReading(Integer hour) {
        this.hour = hour;
        this.saved = false;
        this.isEdited = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DailyReport getDailyReport() { return dailyReport; }
    public void setDailyReport(DailyReport dailyReport) { this.dailyReport = dailyReport; }

    public Integer getHour() { return hour; }
    public void setHour(Integer hour) { this.hour = hour; }

    public Boolean getSaved() { return saved; }
    public void setSaved(Boolean saved) { this.saved = saved; }

    public Boolean getIsEdited() { return isEdited; }
    public void setIsEdited(Boolean isEdited) { this.isEdited = isEdited; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Double getNivelCarga() { return nivelCarga; }
    public void setNivelCarga(Double nivelCarga) { this.nivelCarga = nivelCarga; }

    public Double getNivelDescarga() { return nivelDescarga; }
    public void setNivelDescarga(Double nivelDescarga) { this.nivelDescarga = nivelDescarga; }

    public Double getServAuxKwh() { return servAuxKwh; }
    public void setServAuxKwh(Double servAuxKwh) { this.servAuxKwh = servAuxKwh; }

    public Double getEpsaActarisKwh() { return epsaActarisKwh; }
    public void setEpsaActarisKwh(Double epsaActarisKwh) { this.epsaActarisKwh = epsaActarisKwh; }

    public Double getGenBrutaKwh() { return genBrutaKwh; }
    public void setGenBrutaKwh(Double genBrutaKwh) { this.genBrutaKwh = genBrutaKwh; }

    public Double getPotActivaG1() { return potActivaG1; }
    public void setPotActivaG1(Double potActivaG1) { this.potActivaG1 = potActivaG1; }

    public Double getVoltExcG1() { return voltExcG1; }
    public void setVoltExcG1(Double voltExcG1) { this.voltExcG1 = voltExcG1; }

    public Double getCorrExcG1() { return corrExcG1; }
    public void setCorrExcG1(Double corrExcG1) { this.corrExcG1 = corrExcG1; }

    public Double getVoltG1rst() { return voltG1rst; }
    public void setVoltG1rst(Double voltG1rst) { this.voltG1rst = voltG1rst; }

    public Double getCorrG1faseR() { return corrG1faseR; }
    public void setCorrG1faseR(Double corrG1faseR) { this.corrG1faseR = corrG1faseR; }

    public Double getCorrG1faseS() { return corrG1faseS; }
    public void setCorrG1faseS(Double corrG1faseS) { this.corrG1faseS = corrG1faseS; }

    public Double getCorrG1faseT() { return corrG1faseT; }
    public void setCorrG1faseT(Double corrG1faseT) { this.corrG1faseT = corrG1faseT; }

    public Double getContActarisG1() { return contActarisG1; }
    public void setContActarisG1(Double contActarisG1) { this.contActarisG1 = contActarisG1; }

    public Double getKwhG1() { return kwhG1; }
    public void setKwhG1(Double kwhG1) { this.kwhG1 = kwhG1; }

    public Double getTempTrafoF1() { return tempTrafoF1; }
    public void setTempTrafoF1(Double tempTrafoF1) { this.tempTrafoF1 = tempTrafoF1; }

    public Double getTempTrafoF2() { return tempTrafoF2; }
    public void setTempTrafoF2(Double tempTrafoF2) { this.tempTrafoF2 = tempTrafoF2; }

    public Double getTempTrafoF3() { return tempTrafoF3; }
    public void setTempTrafoF3(Double tempTrafoF3) { this.tempTrafoF3 = tempTrafoF3; }

    public Double getTempG1CojExc() { return tempG1CojExc; }
    public void setTempG1CojExc(Double tempG1CojExc) { this.tempG1CojExc = tempG1CojExc; }

    public Double getTempG1SalidaAire() { return tempG1SalidaAire; }
    public void setTempG1SalidaAire(Double tempG1SalidaAire) { this.tempG1SalidaAire = tempG1SalidaAire; }

    public Double getTempG1EntradaAire() { return tempG1EntradaAire; }
    public void setTempG1EntradaAire(Double tempG1EntradaAire) { this.tempG1EntradaAire = tempG1EntradaAire; }

    public Double getTempG1CojAcoplado() { return tempG1CojAcoplado; }
    public void setTempG1CojAcoplado(Double tempG1CojAcoplado) { this.tempG1CojAcoplado = tempG1CojAcoplado; }

    public Double getTempG1CojNoAcoplado() { return tempG1CojNoAcoplado; }
    public void setTempG1CojNoAcoplado(Double tempG1CojNoAcoplado) { this.tempG1CojNoAcoplado = tempG1CojNoAcoplado; }

    public Double getTempG1CojEmpuje() { return tempG1CojEmpuje; }
    public void setTempG1CojEmpuje(Double tempG1CojEmpuje) { this.tempG1CojEmpuje = tempG1CojEmpuje; }

    public Double getTempG1Aceite() { return tempG1Aceite; }
    public void setTempG1Aceite(Double tempG1Aceite) { this.tempG1Aceite = tempG1Aceite; }

    public Double getTempG1SalidaAireExc() { return tempG1SalidaAireExc; }
    public void setTempG1SalidaAireExc(Double tempG1SalidaAireExc) { this.tempG1SalidaAireExc = tempG1SalidaAireExc; }

    public Double getPotActivaG2() { return potActivaG2; }
    public void setPotActivaG2(Double potActivaG2) { this.potActivaG2 = potActivaG2; }

    public Double getVoltExcG2() { return voltExcG2; }
    public void setVoltExcG2(Double voltExcG2) { this.voltExcG2 = voltExcG2; }

    public Double getCorrExcG2() { return corrExcG2; }
    public void setCorrExcG2(Double corrExcG2) { this.corrExcG2 = corrExcG2; }

    public Double getVoltG2rst() { return voltG2rst; }
    public void setVoltG2rst(Double voltG2rst) { this.voltG2rst = voltG2rst; }

    public Double getCorrG2faseR() { return corrG2faseR; }
    public void setCorrG2faseR(Double corrG2faseR) { this.corrG2faseR = corrG2faseR; }

    public Double getCorrG2faseS() { return corrG2faseS; }
    public void setCorrG2faseS(Double corrG2faseS) { this.corrG2faseS = corrG2faseS; }

    public Double getCorrG2faseT() { return corrG2faseT; }
    public void setCorrG2faseT(Double corrG2faseT) { this.corrG2faseT = corrG2faseT; }

    public Double getTempCojGuiaG2() { return tempCojGuiaG2; }
    public void setTempCojGuiaG2(Double tempCojGuiaG2) { this.tempCojGuiaG2 = tempCojGuiaG2; }

    public Double getTempCojAcopladoT2() { return tempCojAcopladoT2; }
    public void setTempCojAcopladoT2(Double tempCojAcopladoT2) { this.tempCojAcopladoT2 = tempCojAcopladoT2; }

    public Double getContActarisG2() { return contActarisG2; }
    public void setContActarisG2(Double contActarisG2) { this.contActarisG2 = contActarisG2; }

    public Double getKwhG2() { return kwhG2; }
    public void setKwhG2(Double kwhG2) { this.kwhG2 = kwhG2; }

    public Double getTempG2CojExc() { return tempG2CojExc; }
    public void setTempG2CojExc(Double tempG2CojExc) { this.tempG2CojExc = tempG2CojExc; }

    public Double getTempG2SalidaAire() { return tempG2SalidaAire; }
    public void setTempG2SalidaAire(Double tempG2SalidaAire) { this.tempG2SalidaAire = tempG2SalidaAire; }

    public Double getTempG2EntradaAire() { return tempG2EntradaAire; }
    public void setTempG2EntradaAire(Double tempG2EntradaAire) { this.tempG2EntradaAire = tempG2EntradaAire; }

    public Double getTempG2CojAcoplado() { return tempG2CojAcoplado; }
    public void setTempG2CojAcoplado(Double tempG2CojAcoplado) { this.tempG2CojAcoplado = tempG2CojAcoplado; }

    public Double getTempG2CojNoAcoplado() { return tempG2CojNoAcoplado; }
    public void setTempG2CojNoAcoplado(Double tempG2CojNoAcoplado) { this.tempG2CojNoAcoplado = tempG2CojNoAcoplado; }

    public Double getTempG2CojEmpuje() { return tempG2CojEmpuje; }
    public void setTempG2CojEmpuje(Double tempG2CojEmpuje) { this.tempG2CojEmpuje = tempG2CojEmpuje; }

    public Double getTempG2Aceite() { return tempG2Aceite; }
    public void setTempG2Aceite(Double tempG2Aceite) { this.tempG2Aceite = tempG2Aceite; }

    public Double getTempG2NucleoEstator() { return tempG2NucleoEstator; }
    public void setTempG2NucleoEstator(Double tempG2NucleoEstator) { this.tempG2NucleoEstator = tempG2NucleoEstator; }

    public Double getTempG2EstatorFaseU() { return tempG2EstatorFaseU; }
    public void setTempG2EstatorFaseU(Double tempG2EstatorFaseU) { this.tempG2EstatorFaseU = tempG2EstatorFaseU; }

    public Double getTempG2EstatorFaseV() { return tempG2EstatorFaseV; }
    public void setTempG2EstatorFaseV(Double tempG2EstatorFaseV) { this.tempG2EstatorFaseV = tempG2EstatorFaseV; }

    public Double getTempG2EstatorFaseW() { return tempG2EstatorFaseW; }
    public void setTempG2EstatorFaseW(Double tempG2EstatorFaseW) { this.tempG2EstatorFaseW = tempG2EstatorFaseW; }

    public User getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(User lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
