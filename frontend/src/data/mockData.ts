import type { HourlyReading, AuditEntry, AppUser, ColDef } from '../types';

export const DEMO_HOUR = 14;

function rnd(min: number, max: number, dec = 2): string {
  return (min + Math.random() * (max - min)).toFixed(dec);
}

// ── Column definitions (48 columns: 45 manual + 3 calculated) ──────────────
export const COLUMN_DEFS: ColDef[] = [
  // ── Hidráulico ──────────────────────────────────────────────────────────
  { col:'B',  key:'nivelCarga',           label:'Niv. Carga',          unit:'msnm',  editable:true,  group:'Hidráulico',     width:76 },
  { col:'C',  key:'nivelDescarga',        label:'Niv. Descarga',       unit:'msnm',  editable:true,  group:'Hidráulico',     width:80 },

  // ── Medición Frontera ────────────────────────────────────────────────────
  { col:'D',  key:'servAuxKwh',           label:'Serv. Aux',           unit:'kWh',   editable:true,  group:'Med. Frontera',  width:82 },
  { col:'E',  key:'epsaActarisKwh',       label:'EPSA Actaris',        unit:'kWh',   editable:true,  group:'Med. Frontera',  width:88 },
  { col:'F',  key:'genBrutaKwh',          label:'Gen. Bruta',          unit:'kWh',   editable:false, group:'Med. Frontera',  width:84 },

  // ── G-1 Eléctrico ────────────────────────────────────────────────────────
  { col:'G',  key:'potActivaG1',          label:'Pot. Activa',         unit:'kW',    editable:true,  group:'G-1 Eléctrico',  width:76 },
  { col:'H',  key:'voltExcG1',            label:'V. Excit.',           unit:'V',     editable:true,  group:'G-1 Eléctrico',  width:68 },
  { col:'I',  key:'corrExcG1',            label:'I. Excit.',           unit:'A',     editable:true,  group:'G-1 Eléctrico',  width:68 },
  { col:'J',  key:'voltG1rst',            label:'V. RST',              unit:'V',     editable:true,  group:'G-1 Eléctrico',  width:72 },
  { col:'K',  key:'corrG1faseR',          label:'I. Fase R',           unit:'A',     editable:true,  group:'G-1 Eléctrico',  width:68 },
  { col:'L',  key:'corrG1faseS',          label:'I. Fase S',           unit:'A',     editable:true,  group:'G-1 Eléctrico',  width:68 },
  { col:'M',  key:'corrG1faseT',          label:'I. Fase T',           unit:'A',     editable:true,  group:'G-1 Eléctrico',  width:68 },

  // ── Medición G-1 ────────────────────────────────────────────────────────
  { col:'N',  key:'contActarisG1',        label:'Cont. Actaris',       unit:'kWh',   editable:true,  group:'Med. G-1',       width:90 },
  { col:'O',  key:'kwhG1',               label:'KWH G-1',             unit:'kWh',   editable:false, group:'Med. G-1',       width:82 },

  // ── Transformador ────────────────────────────────────────────────────────
  { col:'P',  key:'tempTrafoF1',          label:'Trafo F1',            unit:'°C',    editable:true,  group:'Transformador',  width:66 },
  { col:'Q',  key:'tempTrafoF2',          label:'Trafo F2',            unit:'°C',    editable:true,  group:'Transformador',  width:66 },
  { col:'R',  key:'tempTrafoF3',          label:'Trafo F3',            unit:'°C',    editable:true,  group:'Transformador',  width:66 },

  // ── Temperaturas G-1 ────────────────────────────────────────────────────
  { col:'S',  key:'tempG1CojExc',         label:'Coj. Excit.',         unit:'°C',    editable:true,  group:'Temp. G-1',      width:72 },
  { col:'T',  key:'tempG1SalidaAire',     label:'Sal. Aire',           unit:'°C',    editable:true,  group:'Temp. G-1',      width:68 },
  { col:'U',  key:'tempG1EntradaAire',    label:'Ent. Aire',           unit:'°C',    editable:true,  group:'Temp. G-1',      width:68 },
  { col:'V',  key:'tempG1CojAcoplado',    label:'Coj. Acop.',          unit:'°C',    editable:true,  group:'Temp. G-1',      width:72 },
  { col:'W',  key:'tempG1CojNoAcoplado',  label:'Coj. No Ac.',         unit:'°C',    editable:true,  group:'Temp. G-1',      width:72 },
  { col:'X',  key:'tempG1CojEmpuje',      label:'Coj. Empuje',         unit:'°C',    editable:true,  group:'Temp. G-1',      width:76 },
  { col:'Y',  key:'tempG1Aceite',         label:'Aceite Cuba',         unit:'°C',    editable:true,  group:'Temp. G-1',      width:76 },
  { col:'Z',  key:'tempG1SalidaAireExc',  label:'Sal. Aire Exc.',      unit:'°C',    editable:true,  group:'Temp. G-1',      width:80 },

  // ── G-2 Eléctrico ────────────────────────────────────────────────────────
  { col:'AC', key:'potActivaG2',          label:'Pot. Activa',         unit:'kW',    editable:true,  group:'G-2 Eléctrico',  width:76 },
  { col:'AD', key:'voltExcG2',            label:'V. Excit.',           unit:'V',     editable:true,  group:'G-2 Eléctrico',  width:68 },
  { col:'AE', key:'corrExcG2',            label:'I. Excit.',           unit:'A',     editable:true,  group:'G-2 Eléctrico',  width:68 },
  { col:'AF', key:'voltG2rst',            label:'V. RST',              unit:'V',     editable:true,  group:'G-2 Eléctrico',  width:72 },
  { col:'AG', key:'corrG2faseR',          label:'I. Fase R',           unit:'A',     editable:true,  group:'G-2 Eléctrico',  width:68 },
  { col:'AH', key:'corrG2faseS',          label:'I. Fase S',           unit:'A',     editable:true,  group:'G-2 Eléctrico',  width:68 },
  { col:'AI', key:'corrG2faseT',          label:'I. Fase T',           unit:'A',     editable:true,  group:'G-2 Eléctrico',  width:68 },

  // ── Mecánico G-2 ────────────────────────────────────────────────────────
  { col:'AJ', key:'tempCojGuiaG2',        label:'Coj. Guía G2',        unit:'°C',    editable:true,  group:'Mecánico G-2',   width:82 },
  { col:'AK', key:'tempCojAcopladoT2',    label:'Coj. Acop. T2',       unit:'°C',    editable:true,  group:'Mecánico G-2',   width:84 },

  // ── Medición G-2 ────────────────────────────────────────────────────────
  { col:'AL', key:'contActarisG2',        label:'Cont. Actaris',       unit:'kWh',   editable:true,  group:'Med. G-2',       width:90 },
  { col:'AM', key:'kwhG2',               label:'KWH G-2',             unit:'kWh',   editable:false, group:'Med. G-2',       width:82 },

  // ── Temperaturas G-2 ────────────────────────────────────────────────────
  { col:'AN', key:'tempG2CojExc',         label:'Coj. Excit.',         unit:'°C',    editable:true,  group:'Temp. G-2',      width:72 },
  { col:'AO', key:'tempG2SalidaAire',     label:'Sal. Aire',           unit:'°C',    editable:true,  group:'Temp. G-2',      width:68 },
  { col:'AP', key:'tempG2EntradaAire',    label:'Ent. Aire',           unit:'°C',    editable:true,  group:'Temp. G-2',      width:68 },
  { col:'AQ', key:'tempG2CojAcoplado',    label:'Coj. Acop.',          unit:'°C',    editable:true,  group:'Temp. G-2',      width:72 },
  { col:'AR', key:'tempG2CojNoAcoplado',  label:'Coj. No Ac.',         unit:'°C',    editable:true,  group:'Temp. G-2',      width:72 },
  { col:'AS', key:'tempG2CojEmpuje',      label:'Coj. Empuje',         unit:'°C',    editable:true,  group:'Temp. G-2',      width:76 },
  { col:'AT', key:'tempG2Aceite',         label:'Aceite Cuba',         unit:'°C',    editable:true,  group:'Temp. G-2',      width:76 },

  // ── Estator G-2 ─────────────────────────────────────────────────────────
  { col:'AU', key:'tempG2NucleoEstator',  label:'Núcleo Est.',         unit:'°C',    editable:true,  group:'Estator G-2',    width:80 },
  { col:'AV', key:'tempG2EstatorFaseU',   label:'Est. Fase U',         unit:'°C',    editable:true,  group:'Estator G-2',    width:76 },
  { col:'AW', key:'tempG2EstatorFaseV',   label:'Est. Fase V',         unit:'°C',    editable:true,  group:'Estator G-2',    width:76 },
  { col:'AX', key:'tempG2EstatorFaseW',   label:'Est. Fase W',         unit:'°C',    editable:true,  group:'Estator G-2',    width:76 },
];

export const EDITABLE_COL_DEFS = COLUMN_DEFS.filter(c => c.editable);

export const GROUPS_ORDER = [
  'Hidráulico', 'Med. Frontera',
  'G-1 Eléctrico', 'Med. G-1', 'Transformador', 'Temp. G-1',
  'G-2 Eléctrico', 'Mecánico G-2', 'Med. G-2', 'Temp. G-2', 'Estator G-2',
];

export const GROUP_META: Record<string, { text: string; formCols: number }> = {
  'Hidráulico':    { text: 'var(--c-blue-l)',  formCols: 2 },
  'Med. Frontera': { text: '#818cf8',           formCols: 2 },
  'G-1 Eléctrico': { text: 'var(--c-green)',   formCols: 4 },
  'Med. G-1':      { text: '#39d353',           formCols: 2 },
  'Transformador': { text: '#fb923c',           formCols: 3 },
  'Temp. G-1':     { text: '#f97316',           formCols: 4 },
  'G-2 Eléctrico': { text: 'var(--c-purple)',  formCols: 4 },
  'Mecánico G-2':  { text: '#c084fc',           formCols: 2 },
  'Med. G-2':      { text: '#a78bfa',           formCols: 2 },
  'Temp. G-2':     { text: '#e879f9',           formCols: 4 },
  'Estator G-2':   { text: '#f0abfc',           formCols: 4 },
};

// ── Calculated field formulas ────────────────────────────────────────────────

export function parseNum(val: unknown): number | null {
  if (val === null || val === undefined || val === '') return null;
  if (typeof val === 'number') return isNaN(val) ? null : val;
  if (typeof val !== 'string') return null;
  const trimmed = val.trim();
  if (!trimmed) return null;
  let cleaned = trimmed;
  if (cleaned.includes('.') && cleaned.includes(',')) {
    if (cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')) {
      cleaned = cleaned.replace(/\./g, '').replace(',', '.');
    } else {
      cleaned = cleaned.replace(/,/g, '');
    }
  } else if (cleaned.includes(',')) {
    cleaned = cleaned.replace(',', '.');
  }
  const n = parseFloat(cleaned);
  return isNaN(n) ? null : n;
}

export function findPreviousReading(
  readings: HourlyReading[],
  currentHour: number,
  field: 'epsaActarisKwh' | 'contActarisG1' | 'contActarisG2'
): HourlyReading | { [k in keyof HourlyReading]?: any } | null {
  for (let h = currentHour - 1; h >= 0; h--) {
    const r = readings.find(reading => reading.hour === h);
    if (r && r[field] !== undefined && r[field] !== null && String(r[field]).trim() !== '') {
      return r;
    }
  }
  // Baseline initial counter for hour 0 (matching yesterday 23:00)
  if (field === 'epsaActarisKwh') {
    return { epsaActarisKwh: '4823644.800' };
  }
  if (field === 'contActarisG1') {
    return { contActarisG1: '1234562.300' };
  }
  if (field === 'contActarisG2') {
    return { contActarisG2: '987649.400' };
  }
  return null;
}

export function calcGenBrutaKwh(readings: HourlyReading[], hour: number): string {
  const cur = readings.find(r => r.hour === hour);
  if (!cur) return '';
  const prv = findPreviousReading(readings, hour, 'epsaActarisKwh');
  const curVal = parseNum(cur.epsaActarisKwh);
  const prvVal = prv ? parseNum(prv.epsaActarisKwh) : null;
  if (curVal !== null && prvVal !== null) {
    const delta = curVal - prvVal;
    if (delta >= 0) {
      return Math.round(delta * 2400).toLocaleString('es-CO');
    }
  }
  if (cur.genBrutaKwh && String(cur.genBrutaKwh).trim() !== '') {
    const num = parseNum(cur.genBrutaKwh);
    return num !== null ? Math.round(num).toLocaleString('es-CO') : String(cur.genBrutaKwh);
  }
  return '';
}

export function calcKwhG1(readings: HourlyReading[], hour: number): string {
  const cur = readings.find(r => r.hour === hour);
  if (!cur) return '';
  const prv = findPreviousReading(readings, hour, 'contActarisG1');
  const curVal = parseNum(cur.contActarisG1);
  const prvVal = prv ? parseNum(prv.contActarisG1) : null;
  if (curVal !== null && prvVal !== null) {
    const delta = curVal - prvVal;
    if (delta >= 0) {
      return Math.round(delta * 1363.63).toLocaleString('es-CO');
    }
  }
  if (cur.kwhG1 && String(cur.kwhG1).trim() !== '') {
    const num = parseNum(cur.kwhG1);
    return num !== null ? Math.round(num).toLocaleString('es-CO') : String(cur.kwhG1);
  }
  return '';
}

export function calcKwhG2(readings: HourlyReading[], hour: number): string {
  const cur = readings.find(r => r.hour === hour);
  if (!cur) return '';
  const prv = findPreviousReading(readings, hour, 'contActarisG2');
  const curVal = parseNum(cur.contActarisG2);
  const prvVal = prv ? parseNum(prv.contActarisG2) : null;
  if (curVal !== null && prvVal !== null) {
    const delta = curVal - prvVal;
    if (delta >= 0) {
      return Math.round(delta * 1363.63).toLocaleString('es-CO');
    }
  }
  if (cur.kwhG2 && String(cur.kwhG2).trim() !== '') {
    const num = parseNum(cur.kwhG2);
    return num !== null ? Math.round(num).toLocaleString('es-CO') : String(cur.kwhG2);
  }
  return '';
}

/** Get all 3 calculated values for a row, given the full readings array */
export function getCalcValues(readings: HourlyReading[], hour: number) {
  return {
    genBrutaKwh: calcGenBrutaKwh(readings, hour),
    kwhG1:       calcKwhG1(readings, hour),
    kwhG2:       calcKwhG2(readings, hour),
  };
}

// ── Mock data generator ──────────────────────────────────────────────────────

export function generateInitialReadings(): HourlyReading[] {
  const readings: HourlyReading[] = [];

  // Starting counter values (yesterday 23:00 baseline)
  let servAux      = 125840.00;
  let epsaActaris  = 4823644.800;
  let contG1       = 1234562.300;
  let contG2       = 987649.400;

  for (let h = 0; h < 24; h++) {
    if (h >= DEMO_HOUR) {
      readings.push(emptyReading(h));
      continue;
    }

    const prevEpsa = epsaActaris;
    const prevG1   = contG1;
    const prevG2   = contG2;

    // Increment counters first
    const dEpsa = parseFloat(rnd(5.8, 7.4, 3));
    const dG1   = parseFloat(rnd(4.1, 5.9, 3));
    const dG2   = parseFloat(rnd(3.9, 5.7, 3));
    servAux     += parseFloat(rnd(28, 46, 1));
    epsaActaris += dEpsa;
    contG1      += dG1;
    contG2      += dG2;

    const potG1 = parseFloat(rnd(4800, 7800, 0));
    const potG2 = parseFloat(rnd(4600, 7500, 0));

    const genBruta = Math.round((epsaActaris - prevEpsa) * 2400).toLocaleString('es-CO');
    const kG1      = Math.round((contG1 - prevG1) * 1363.63).toLocaleString('es-CO');
    const kG2      = Math.round((contG2 - prevG2) * 1363.63).toLocaleString('es-CO');

    readings.push({
      hour: h,
      saved: true,

      nivelCarga:             rnd(645.8, 647.5),
      nivelDescarga:          rnd(512.4, 514.1),

      servAuxKwh:             servAux.toFixed(2),
      epsaActarisKwh:         epsaActaris.toFixed(3),
      genBrutaKwh:            genBruta,

      potActivaG1:            potG1.toFixed(0),
      voltExcG1:              rnd(95, 112, 1),
      corrExcG1:              rnd(220, 340, 0),
      voltG1rst:              rnd(13780, 13850, 0),
      corrG1faseR:            rnd(240, 410, 0),
      corrG1faseS:            rnd(238, 412, 0),
      corrG1faseT:            rnd(241, 409, 0),
      contActarisG1:          contG1.toFixed(3),
      kwhG1:                  kG1,

      tempTrafoF1:            rnd(46, 62, 1),
      tempTrafoF2:            rnd(47, 63, 1),
      tempTrafoF3:            rnd(45, 61, 1),

      tempG1CojExc:           rnd(44, 58, 1),
      tempG1SalidaAire:       rnd(38, 52, 1),
      tempG1EntradaAire:      rnd(32, 44, 1),
      tempG1CojAcoplado:      rnd(42, 56, 1),
      tempG1CojNoAcoplado:    rnd(40, 54, 1),
      tempG1CojEmpuje:        rnd(43, 57, 1),
      tempG1Aceite:           rnd(45, 60, 1),
      tempG1SalidaAireExc:    rnd(36, 50, 1),

      potActivaG2:            potG2.toFixed(0),
      voltExcG2:              rnd(93, 110, 1),
      corrExcG2:              rnd(210, 330, 0),
      voltG2rst:              rnd(13770, 13840, 0),
      corrG2faseR:            rnd(230, 400, 0),
      corrG2faseS:            rnd(228, 402, 0),
      corrG2faseT:            rnd(231, 398, 0),
      tempCojGuiaG2:          rnd(41, 56, 1),
      tempCojAcopladoT2:      rnd(40, 54, 1),
      contActarisG2:          contG2.toFixed(3),
      kwhG2:                  kG2,

      tempG2CojExc:           rnd(43, 57, 1),
      tempG2SalidaAire:       rnd(37, 51, 1),
      tempG2EntradaAire:      rnd(31, 43, 1),
      tempG2CojAcoplado:      rnd(41, 55, 1),
      tempG2CojNoAcoplado:    rnd(39, 53, 1),
      tempG2CojEmpuje:        rnd(42, 56, 1),
      tempG2Aceite:           rnd(44, 59, 1),

      tempG2NucleoEstator:    rnd(62, 78, 1),
      tempG2EstatorFaseU:     rnd(64, 80, 1),
      tempG2EstatorFaseV:     rnd(63, 79, 1),
      tempG2EstatorFaseW:     rnd(65, 81, 1),
    });
  }

  return readings;
}

function emptyReading(hour: number): HourlyReading {
  const empty = '' as const;
  return {
    hour, saved: false,
    nivelCarga: empty, nivelDescarga: empty,
    servAuxKwh: empty, epsaActarisKwh: empty,
    potActivaG1: empty, voltExcG1: empty, corrExcG1: empty,
    voltG1rst: empty, corrG1faseR: empty, corrG1faseS: empty, corrG1faseT: empty,
    contActarisG1: empty,
    tempTrafoF1: empty, tempTrafoF2: empty, tempTrafoF3: empty,
    tempG1CojExc: empty, tempG1SalidaAire: empty, tempG1EntradaAire: empty,
    tempG1CojAcoplado: empty, tempG1CojNoAcoplado: empty,
    tempG1CojEmpuje: empty, tempG1Aceite: empty, tempG1SalidaAireExc: empty,
    potActivaG2: empty, voltExcG2: empty, corrExcG2: empty,
    voltG2rst: empty, corrG2faseR: empty, corrG2faseS: empty, corrG2faseT: empty,
    tempCojGuiaG2: empty, tempCojAcopladoT2: empty,
    contActarisG2: empty,
    tempG2CojExc: empty, tempG2SalidaAire: empty, tempG2EntradaAire: empty,
    tempG2CojAcoplado: empty, tempG2CojNoAcoplado: empty,
    tempG2CojEmpuje: empty, tempG2Aceite: empty,
    tempG2NucleoEstator: empty, tempG2EstatorFaseU: empty,
    tempG2EstatorFaseV: empty, tempG2EstatorFaseW: empty,
  };
}

// ── Static data ──────────────────────────────────────────────────────────────

export const initialAuditLog: AuditEntry[] = [
  {
    id: 1, timestamp: '2026-09-19 13:47:22', operador: 'C. Mendoza',
    hora: '11:00', campo: 'Pot. Activa G-1 (kW)',
    valorAnterior: '5480', valorNuevo: '5720',
    justificacion: 'Error de transcripción en lectura inicial. Corregido según instrumento de panel.',
  },
  {
    id: 2, timestamp: '2026-09-19 11:12:05', operador: 'C. Mendoza',
    hora: '10:00', campo: 'Niv. Carga (msnm)',
    valorAnterior: '646.12', valorNuevo: '646.35',
    justificacion: 'Se leyó nivel incorrecto del limnímetro. Corregido con segunda lectura.',
  },
  {
    id: 3, timestamp: '2026-09-19 08:55:41', operador: 'R. Torres',
    hora: '07:00', campo: 'Coj. Excit. G-1 (°C)',
    valorAnterior: '71.4', valorNuevo: '54.2',
    justificacion: 'Sensor presentó lectura anómala transitoria. Confirmado con termómetro portátil.',
  },
  {
    id: 4, timestamp: '2026-09-19 07:30:18', operador: 'R. Torres',
    hora: '06:00', campo: 'EPSA Actaris (kWh)',
    valorAnterior: '4823695.412', valorNuevo: '4823698.115',
    justificacion: 'Diferencia detectada en cruce de turno. Ajuste aprobado por supervisor.',
  },
  {
    id: 5, timestamp: '2026-09-19 02:14:55', operador: 'F. Ríos',
    hora: '01:00', campo: 'V. RST G-1 (V)',
    valorAnterior: '13650', valorNuevo: '13802',
    justificacion: 'Lectura durante transitorio de carga. Corregido al estabilizarse el sistema.',
  },
];

export const initialUsers: AppUser[] = [
  { id: 1, nombre: 'Administrador Sistema', usuario: 'admin',      rol: 'admin',    activo: true },
  { id: 2, nombre: 'Carlos Mendoza',        usuario: 'c.mendoza',  rol: 'operador', activo: true,  turno: 'Turno A (06:00–18:00)' },
  { id: 3, nombre: 'Ramiro Torres',         usuario: 'r.torres',   rol: 'operador', activo: true,  turno: 'Turno B (18:00–06:00)' },
  { id: 4, nombre: 'Felipe Ríos',           usuario: 'f.rios',     rol: 'operador', activo: true,  turno: 'Turno B (18:00–06:00)' },
  { id: 5, nombre: 'Diana Castillo',        usuario: 'd.castillo', rol: 'operador', activo: false, turno: '—' },
];

// ── Helpers ───────────────────────────────────────────────────────────────────

export function getShift(hour: number): { label: string; range: string; id: 'A' | 'B' } {
  if (hour >= 6 && hour < 18) return { label: 'Turno A', range: '06:00 – 18:00', id: 'A' };
  return { label: 'Turno B', range: '18:00 – 06:00', id: 'B' };
}

export function fmtHour(h: number): string {
  return `${String(h).padStart(2, '0')}:00`;
}
