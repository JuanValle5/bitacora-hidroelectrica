export interface HourlyReading {
  hour: number;
  saved: boolean;
  isEdited?: boolean;
  observations?: string;

  // B — Hidráulico
  nivelCarga: string;
  nivelDescarga: string;

  // D, E — Medición Frontera
  servAuxKwh: string;
  epsaActarisKwh: string;
  // F — GENERACION BRUTA KWH (calculada: (E_actual - E_anterior) * 2400)
  genBrutaKwh?: string;

  // G-1 Eléctrico
  potActivaG1: string;
  voltExcG1: string;
  corrExcG1: string;
  voltG1rst: string;
  corrG1faseR: string;
  corrG1faseS: string;
  corrG1faseT: string;

  // N — Medición G-1
  contActarisG1: string;
  // O — KWH G-1 (calculada: (N_actual - N_anterior) * 1363.63)
  kwhG1?: string;

  // Transformador 1.500 KVA
  tempTrafoF1: string;
  tempTrafoF2: string;
  tempTrafoF3: string;

  // Temperaturas G-1
  tempG1CojExc: string;
  tempG1SalidaAire: string;
  tempG1EntradaAire: string;
  tempG1CojAcoplado: string;
  tempG1CojNoAcoplado: string;
  tempG1CojEmpuje: string;
  tempG1Aceite: string;
  tempG1SalidaAireExc: string;

  // G-2 Eléctrico
  potActivaG2: string;
  voltExcG2: string;
  corrExcG2: string;
  voltG2rst: string;
  corrG2faseR: string;
  corrG2faseS: string;
  corrG2faseT: string;

  // Mecánico G-2
  tempCojGuiaG2: string;
  tempCojAcopladoT2: string;

  // AL — Medición G-2
  contActarisG2: string;
  // AM — KWH G-2 (calculada: (AL_actual - AL_anterior) * 1363.63)
  kwhG2?: string;

  // Temperaturas G-2
  tempG2CojExc: string;
  tempG2SalidaAire: string;
  tempG2EntradaAire: string;
  tempG2CojAcoplado: string;
  tempG2CojNoAcoplado: string;
  tempG2CojEmpuje: string;
  tempG2Aceite: string;

  // Estator G-2
  tempG2NucleoEstator: string;
  tempG2EstatorFaseU: string;
  tempG2EstatorFaseV: string;
  tempG2EstatorFaseW: string;
}

export interface AuditEntry {
  id: number;
  timestamp: string;
  operador: string;
  hora: string;
  campo: string;
  valorAnterior: string;
  valorNuevo: string;
  justificacion: string;
}

export interface AppUser {
  id: number;
  nombre: string;
  usuario: string;
  rol: 'admin' | 'operador';
  activo: boolean;
  turno?: string;
}

export interface PendingEdit {
  hour: number;
  field: keyof HourlyReading;
  fieldLabel: string;
  oldValue: string;
  newValue: string;
}

export type ViewType = 'login' | 'admin' | 'operator';
export type UserRole = 'admin' | 'operador';

export interface ColDef {
  key: string;
  label: string;
  unit: string;
  editable: boolean;
  group: string;
  width: number;
  col: string; // Excel column letter reference
}

export interface DailyReportData {
  reportId: number;
  date: string;
  status: string;
  savedHoursCount: number;
  totalGenBrutaKwh: number;
  totalKwhG1: number;
  totalKwhG2: number;
  totalKwhCombined: number;
  readings: HourlyReading[];
}

export interface DashboardMetrics {
  date: string;
  totalGenerationMwh: number;
  totalGenerationKwh: number;
  averagePowerMw: number;
  activeShiftLabel: string;
  activeShiftRange: string;
  completedHours: number;
  totalHours: number;
  completionPercentage: number;
}

export interface ShiftInfo {
  id: string;
  label: string;
  range: string;
  activeOperatorId?: number;
  activeOperatorName?: string;
  activeOperatorUsername?: string;
}

