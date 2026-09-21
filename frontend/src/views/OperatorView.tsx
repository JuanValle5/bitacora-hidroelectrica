import { useState, useEffect } from 'react';
import type { HourlyReading, AuditEntry, PendingEdit, AppUser, ShiftInfo } from '../types';
import {
  generateInitialReadings, COLUMN_DEFS, EDITABLE_COL_DEFS,
  GROUPS_ORDER, GROUP_META,
  DEMO_HOUR, getShift, fmtHour,
  getCalcValues,
} from '../data/mockData';
import {
  apiGetDailyReport,
  apiRegisterReading,
  apiUpdateReading,
  apiGetUsers,
  apiRecordHandoff,
  apiGetCurrentShift,
} from '../api/client';
import JustifyModal from '../components/JustifyModal';

interface Props {
  userName: string;
  currentUser?: AppUser | null;
  onLogout: () => void;
  addAuditEntry: (e: Omit<AuditEntry, 'id'>) => void;
}

export default function OperatorView({ userName, currentUser, onLogout, addAuditEntry }: Props) {
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [readings, setReadings] = useState<HourlyReading[]>(generateInitialReadings);
  const [pending, setPending]   = useState<PendingEdit | null>(null);
  const [observations, setObservations] = useState('');
  const [showHandoff, setShowHandoff]   = useState(false);
  const [handoffTo, setHandoffTo]       = useState('');
  const [handoffDone, setHandoffDone]   = useState(false);
  const [tab, setTab]                   = useState<'form' | 'grid'>('form');
  const [submitFlash, setSubmitFlash]   = useState(false);
  const [formHour, setFormHour]         = useState<number>(DEMO_HOUR);
  const [formValues, setFormValues]     = useState<Partial<HourlyReading>>({});
  const [apiError, setApiError]         = useState<string | null>(null);
  const [operatorsList, setOperatorsList] = useState<AppUser[]>([]);
  const [currentShiftInfo, setCurrentShiftInfo] = useState<ShiftInfo | null>(null);

  useEffect(() => {
    async function loadData() {
      try {
        const report = await apiGetDailyReport(selectedDate);
        if (report && report.readings && report.readings.length > 0) {
          setReadings(report.readings);
        }
      } catch (err: any) {
        console.warn('Backend report load warning:', err.message);
      }
      try {
        const users = await apiGetUsers();
        setOperatorsList(users.filter(u => u.rol === 'operador' && u.activo));
      } catch (err: any) {
        console.warn('Backend users load warning:', err.message);
      }
      try {
        const shiftData = await apiGetCurrentShift();
        setCurrentShiftInfo(shiftData);
      } catch (err: any) {
        console.warn('Backend shift info load warning:', err.message);
      }
    }
    loadData();
  }, [selectedDate]);

  const shift    = getShift(formHour);
  const savedRows = readings.filter(r => r.saved);

  // Determinar si el usuario autenticado es el operador activo en turno
  const isOnDuty = currentUser?.rol === 'admin' || (
    currentShiftInfo
      ? (
          (currentShiftInfo.activeOperatorId && currentUser?.id === currentShiftInfo.activeOperatorId) ||
          (currentShiftInfo.activeOperatorUsername && currentUser?.usuario === currentShiftInfo.activeOperatorUsername) ||
          (currentShiftInfo.activeOperatorName && userName && currentShiftInfo.activeOperatorName.toLowerCase() === userName.toLowerCase())
        )
      : true
  );

  const totalKwh = readings.reduce((s, r) => {
    const c = getCalcValues(readings, r.hour);
    const g1 = parseInt((c.kwhG1 || r.kwhG1 || '').replace(/\./g, '').replace(',', '.')) || 0;
    const g2 = parseInt((c.kwhG2 || r.kwhG2 || '').replace(/\./g, '').replace(',', '.')) || 0;
    return s + g1 + g2;
  }, 0);

  // ── Form helpers ─────────────────────────────────────────────────────────────

  function loadHourIntoForm(h: number) {
    setFormHour(h);
    setApiError(null);
    const existing = readings.find(r => r.hour === h);
    if (existing?.saved) {
      const vals: Partial<HourlyReading> = {};
      EDITABLE_COL_DEFS.forEach(f => {
        (vals as Record<string, unknown>)[f.key] = existing[f.key as keyof HourlyReading];
      });
      setFormValues(vals);
      setObservations(existing.observations || '');
    } else {
      setFormValues({});
      setObservations('');
    }
  }

  function handleFormChange(key: keyof HourlyReading, value: string) {
    setFormValues(prev => ({ ...prev, [key]: value }));
  }

  function handleFormSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!isOnDuty) {
      setApiError("No está autorizado para registrar o modificar lecturas: el turno activo le pertenece a " + (currentShiftInfo?.activeOperatorName || 'otro operador') + ".");
      return;
    }

    setApiError(null);
    const existing = readings.find(r => r.hour === formHour);
    if (existing?.saved) {
      const firstChanged = EDITABLE_COL_DEFS.find(f => {
        const o = existing[f.key as keyof HourlyReading] as string || '';
        const n = formValues[f.key as keyof HourlyReading] as string || '';
        return o !== n && n !== '';
      });
      if (firstChanged) {
        setPending({
          hour: formHour,
          field: firstChanged.key as keyof HourlyReading,
          fieldLabel: `${firstChanged.label} (${firstChanged.unit})`,
          oldValue: existing[firstChanged.key as keyof HourlyReading] as string || '',
          newValue: formValues[firstChanged.key as keyof HourlyReading] as string || '',
        });
        return;
      }
    }
    applyFormValues();
  }

  async function applyFormValues(justification?: string) {
    if (!isOnDuty) {
      setApiError("No está autorizado para registrar o modificar lecturas: el turno activo le pertenece a " + (currentShiftInfo?.activeOperatorName || 'otro operador') + ".");
      return;
    }

    const existing = readings.find(r => r.hour === formHour);

    try {
      setApiError(null);
      if (existing?.saved) {
        if (!justification || justification.trim().length < 10) {
          setApiError('La justificación debe tener al menos 10 caracteres.');
          return;
        }

        const updated = await apiUpdateReading(selectedDate, formHour, { ...formValues, observations }, justification);
        setReadings(prev => prev.map(r => (r.hour === formHour ? updated : r)));

        if (pending) {
          addAuditEntry({
            timestamp: new Date().toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
            operador: userName,
            hora: fmtHour(formHour),
            campo: pending.fieldLabel,
            valorAnterior: pending.oldValue || '—',
            valorNuevo: pending.newValue,
            justificacion: justification || '',
          });
        }
      } else {
        const saved = await apiRegisterReading(selectedDate, { hour: formHour, ...formValues, observations });
        setReadings(prev => prev.map(r => (r.hour === formHour ? saved : r)));
      }

      setPending(null);
      setSubmitFlash(true);
      setTimeout(() => setSubmitFlash(false), 1400);
      setTab('grid');
    } catch (err: any) {
      setApiError(err.message || 'Error al guardar los datos en el servidor.');
    }
  }

  // Build a temp readings array for form preview (inject form values as current hour)
  function buildPreviewReadings(): HourlyReading[] {
    return readings.map(r => {
      if (r.hour !== formHour) return r;
      const preview = {
        ...r,
        saved: true,
        genBrutaKwh: undefined,
        kwhG1: undefined,
        kwhG2: undefined,
      } as HourlyReading;
      EDITABLE_COL_DEFS.forEach(f => {
        const v = formValues[f.key as keyof HourlyReading];
        if (v !== undefined && v !== '') {
          (preview as unknown as Record<string, string>)[f.key] = v as string;
        }
      });
      return preview;
    });
  }

  const previewReadings = buildPreviewReadings();
  const previewCalc     = getCalcValues(previewReadings, formHour);

  // ── Navbar ───────────────────────────────────────────────────────────────────

  const navbar = (
    <header className="flex items-center justify-between px-5 h-12 shrink-0"
      style={{ backgroundColor: 'var(--c-s1)', borderBottom: '1px solid var(--c-b1)' }}>
      <div className="flex items-center gap-3">
        <div className="w-6 h-6 rounded-md flex items-center justify-center"
          style={{ backgroundColor: 'var(--c-s2)', border: '1px solid var(--c-b1)' }}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" className="w-3.5 h-3.5" style={{ color: 'var(--c-blue)' }}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
          </svg>
        </div>
        <span className="text-xs font-bold tracking-tight" style={{ color: 'var(--c-t1)' }}>Bitácora Hidroeléctrica</span>
        <span className="text-xs px-2 py-0.5 rounded font-semibold font-mono"
          style={{ backgroundColor: 'rgba(47,129,247,0.12)', color: 'var(--c-blue)', border: '1px solid rgba(47,129,247,0.25)' }}>
          Operador
        </span>
      </div>
      <div className="flex items-center gap-4">
        <input
          type="date"
          value={selectedDate}
          onChange={e => setSelectedDate(e.target.value)}
          className="text-xs font-mono rounded px-2 py-1"
          style={{ backgroundColor: 'var(--c-s2)', border: '1px solid var(--c-b1)', color: 'var(--c-t2)', outline: 'none', cursor: 'pointer' }}
        />
        <span className="text-xs" style={{ color: 'var(--c-t3)' }}>{userName}</span>
        <button onClick={onLogout}
          className="text-xs px-3 py-1 rounded-md font-semibold"
          style={{ backgroundColor: 'var(--c-s2)', color: 'var(--c-t3)', border: '1px solid var(--c-b1)', cursor: 'pointer' }}
          onMouseEnter={e => (e.currentTarget.style.color = 'var(--c-red)')}
          onMouseLeave={e => (e.currentTarget.style.color = 'var(--c-t3)')}>
          Salir
        </button>
      </div>
    </header>
  );

  // ── Shift header ─────────────────────────────────────────────────────────────

  const shiftBar = (
    <div className="px-5 py-2 flex items-center gap-5 shrink-0"
      style={{ backgroundColor: 'var(--c-s1)', borderBottom: '1px solid var(--c-b1)' }}>
      <div className="flex items-center gap-2">
        <span className="w-2 h-2 rounded-full pulse-dot"
          style={{ backgroundColor: isOnDuty ? 'var(--c-green)' : 'var(--c-amber)' }} />
        <div>
          <div className="text-xs font-bold" style={{ color: 'var(--c-t1)' }}>
            {currentShiftInfo?.label || shift.label}
          </div>
          <div className="text-xs font-mono" style={{ color: 'var(--c-t4)' }}>
            {currentShiftInfo?.range || shift.range}
          </div>
        </div>
      </div>
      {[
        ['Operador en Turno', currentShiftInfo?.activeOperatorName || userName],
        ['Mi Rol', isOnDuty ? 'Activo en Turno' : 'Observador / Relevado'],
        ['Registradas', `${savedRows.length} / 24 h`],
        ['Gen. acumulada', `${totalKwh.toLocaleString('es-CO')} kWh`],
      ].map(([label, val]) => (
        <div key={label} className="flex items-center gap-4">
          <div style={{ width: 1, height: 24, backgroundColor: 'var(--c-b1)' }} />
          <div>
            <div className="text-xs" style={{ color: 'var(--c-t4)' }}>{label}</div>
            <div className="text-xs font-semibold" style={{ color: 'var(--c-t1)' }}>{val}</div>
          </div>
        </div>
      ))}
      <div className="ml-auto">
        <button
          onClick={() => setShowHandoff(true)}
          disabled={!isOnDuty}
          title={!isOnDuty ? 'Solo el operador actualmente en turno puede realizar la entrega de turno' : undefined}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          style={{
            backgroundColor: isOnDuty ? 'var(--c-green-bg)' : 'var(--c-s2)',
            color: isOnDuty ? 'var(--c-green)' : 'var(--c-t5)',
            border: `1px solid ${isOnDuty ? 'rgba(63,185,80,0.3)' : 'var(--c-b1)'}`,
            cursor: isOnDuty ? 'pointer' : 'not-allowed',
          }}>
          Entregar Turno
        </button>
      </div>
    </div>
  );

  // ── Tab bar ──────────────────────────────────────────────────────────────────

  const tabBar = (
    <div className="flex shrink-0" style={{ backgroundColor: 'var(--c-bg)', borderBottom: '1px solid var(--c-b1)' }}>
      {([
        { key: 'form' as const, label: 'Ingreso de Lectura' },
        { key: 'grid' as const, label: 'Cuadrícula de Registro', count: savedRows.length },
      ]).map(t => (
        <button key={t.key} onClick={() => setTab(t.key)}
          className="px-5 py-2.5 text-xs font-semibold flex items-center gap-2"
          style={{
            color: tab === t.key ? 'var(--c-blue)' : 'var(--c-t4)',
            borderBottom: tab === t.key ? '2px solid var(--c-blue)' : '2px solid transparent',
            marginBottom: -1, background: 'none',
            borderTop: 'none', borderLeft: 'none', borderRight: 'none',
            cursor: 'pointer',
          }}>
          {t.label}
          {'count' in t && (
            <span style={{ fontSize: 10, padding: '1px 6px', borderRadius: 8, backgroundColor: 'var(--c-s2)', color: 'var(--c-t3)', fontFamily: 'var(--font-mono)' }}>
              {t.count}
            </span>
          )}
        </button>
      ))}
    </div>
  );

  // ── Form tab ─────────────────────────────────────────────────────────────────

  const formTab = (
    <div className="flex-1 overflow-auto p-5">
      <div className="max-w-4xl mx-auto space-y-4">

        {/* Warning banner if not on duty */}
        {!isOnDuty && (
          <div className="rounded-lg p-3.5 flex items-start gap-3 text-xs"
            style={{ backgroundColor: 'rgba(227,179,65,0.12)', border: '1px solid rgba(227,179,65,0.3)', color: 'var(--c-amber)' }}>
            <svg viewBox="0 0 16 16" fill="currentColor" className="w-4 h-4 shrink-0 mt-0.5">
              <path fillRule="evenodd" d="M8 1a7 7 0 100 14A7 7 0 008 1zM8 3a.75.75 0 01.75.75v3.69l2.28 2.28a.75.75 0 11-1.06 1.06L7.47 8.28A.75.75 0 017.25 7.5v-3.75A.75.75 0 018 3z" clipRule="evenodd"/>
            </svg>
            <div>
              <div className="font-bold">Turno asignado a: {currentShiftInfo?.activeOperatorName || 'otro operador'}</div>
              <div className="mt-0.5 opacity-90">
                Usted se encuentra en modo de consulta (solo lectura). De acuerdo con la regla de turnos rotativos de la central, únicamente el operador en turno activo puede registrar o modificar lecturas horarias.
              </div>
            </div>
          </div>
        )}

        {/* Hour selector */}
        <div className="rounded-lg p-4" style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b1)' }}>
          <div className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: 'var(--c-t3)' }}>
            Hora a registrar
          </div>
          <div className="flex flex-wrap gap-1">
            {Array.from({ length: 24 }, (_, h) => {
              const saved   = readings[h]?.saved;
              const active  = h === formHour;
              const isNow   = h === DEMO_HOUR;
              return (
                <button key={h} onClick={() => loadHourIntoForm(h)}
                  className="font-mono text-xs px-2.5 py-1.5 rounded transition-all"
                  style={{
                    backgroundColor: active ? 'var(--c-blue)' : saved ? 'var(--c-s2)' : 'var(--c-bg)',
                    color: active ? '#fff' : saved ? 'var(--c-blue-l)' : 'var(--c-t5)',
                    border: isNow && !active
                      ? '1px solid rgba(227,179,65,0.5)'
                      : active
                      ? '1px solid var(--c-blue)'
                      : saved
                      ? '1px solid var(--c-b1)'
                      : '1px solid var(--c-b2)',
                    fontWeight: active ? 700 : 400,
                    cursor: 'pointer',
                  }}>
                  {fmtHour(h)}
                  {saved && <span className="ml-1 text-xs opacity-70">✓</span>}
                </button>
              );
            })}
          </div>
        </div>

        {/* Error banner */}
        {apiError && (
          <div className="p-3 rounded-lg flex items-center gap-2 text-xs"
            style={{ backgroundColor: 'var(--c-red-bg)', color: 'var(--c-red)', border: '1px solid rgba(248,81,73,0.3)' }}>
            <svg viewBox="0 0 16 16" fill="currentColor" className="w-4 h-4 shrink-0">
              <path d="M8 1.5a6.5 6.5 0 100 13 6.5 6.5 0 000-13zM0 8a8 8 0 1116 0A8 8 0 010 8zm9 3a1 1 0 11-2 0 1 1 0 012 0zm-.25-6.25a.75.75 0 00-1.5 0v3.5a.75.75 0 001.5 0v-3.5z"/>
            </svg>
            <span className="font-semibold">{apiError}</span>
          </div>
        )}
        <form onSubmit={handleFormSubmit}>
          <div className="rounded-lg overflow-hidden" style={{ border: '1px solid var(--c-b1)' }}>

            {/* Form header */}
            <div className="flex items-center justify-between px-4 py-3"
              style={{ backgroundColor: 'var(--c-s2)', borderBottom: '1px solid var(--c-b1)' }}>
              <div className="flex items-center gap-3">
                <span className="font-mono font-bold text-xl" style={{ color: 'var(--c-t1)', letterSpacing: '-0.02em' }}>
                  {fmtHour(formHour)}
                </span>
                <span className="text-xs px-2 py-0.5 rounded"
                  style={{
                    backgroundColor: readings[formHour]?.saved ? 'rgba(63,185,80,0.1)' : 'rgba(227,179,65,0.1)',
                    color: readings[formHour]?.saved ? 'var(--c-green)' : 'var(--c-amber)',
                    border: `1px solid ${readings[formHour]?.saved ? 'rgba(63,185,80,0.3)' : 'rgba(227,179,65,0.3)'}`,
                  }}>
                  {readings[formHour]?.saved ? 'Editando registro existente' : 'Hora sin datos'}
                </span>
              </div>
              <button type="submit"
                disabled={!isOnDuty}
                className="flex items-center gap-2 px-4 py-2 rounded-md text-xs font-bold transition-all disabled:opacity-40 disabled:cursor-not-allowed"
                style={{
                  backgroundColor: !isOnDuty ? 'var(--c-s2)' : submitFlash ? 'var(--c-green-bg)' : 'var(--c-blue)',
                  color: !isOnDuty ? 'var(--c-t5)' : submitFlash ? 'var(--c-green)' : '#fff',
                  border: !isOnDuty ? '1px solid var(--c-b1)' : submitFlash ? '1px solid rgba(63,185,80,0.4)' : 'none',
                  cursor: !isOnDuty ? 'not-allowed' : 'pointer',
                }}>
                {!isOnDuty ? 'Sin Turno Activo' : submitFlash ? '✓ Guardado' : 'Registrar Hora'}
              </button>
            </div>

            {/* Field groups — only editable fields */}
            {GROUPS_ORDER.map(group => {
              const fields = EDITABLE_COL_DEFS.filter(f => f.group === group);
              if (!fields.length) return null;
              const meta = GROUP_META[group];
              return (
                <div key={group} style={{ borderBottom: '1px solid var(--c-b1)' }}>
                  <div className="px-4 py-1.5 text-xs font-bold uppercase tracking-widest flex items-center gap-2"
                    style={{ backgroundColor: 'var(--c-s2)', color: meta.text, letterSpacing: '0.06em', borderBottom: '1px solid var(--c-b3)' }}>
                    {group}
                  </div>
                  <div className="p-4 grid gap-3" style={{ backgroundColor: 'var(--c-s1)', gridTemplateColumns: `repeat(${meta.formCols}, 1fr)` }}>
                    {fields.map(f => (
                      <div key={f.key}>
                        <label className="block text-xs font-semibold mb-1" style={{ color: 'var(--c-t3)' }}>
                          <span style={{ color: 'var(--c-t5)', fontFamily: 'var(--font-mono)', marginRight: 4 }}>{f.col}</span>
                          {f.label}
                          <span className="ml-1 font-normal" style={{ color: 'var(--c-t5)' }}>({f.unit})</span>
                        </label>
                        <input
                          type="text"
                          inputMode="decimal"
                          disabled={!isOnDuty}
                          value={(formValues[f.key as keyof HourlyReading] as string) ?? ''}
                          onChange={e => handleFormChange(f.key as keyof HourlyReading, e.target.value)}
                          placeholder="—"
                          className={`w-full rounded-md px-3 py-2 text-sm font-mono ${!isOnDuty ? 'opacity-60 cursor-not-allowed' : ''}`}
                          style={{
                            backgroundColor: 'var(--c-bg)',
                            border: '1px solid var(--c-b1)',
                            color: 'var(--c-t1)',
                            outline: 'none',
                          }}
                          onFocus={e => { if (isOnDuty) { e.target.style.borderColor = 'var(--c-blue)'; e.target.style.boxShadow = '0 0 0 3px rgba(47,129,247,0.12)'; } }}
                          onBlur={e =>  { e.target.style.borderColor = 'var(--c-b1)';   e.target.style.boxShadow = 'none'; }}
                        />
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}

            {/* Calculated preview */}
            <div style={{ backgroundColor: 'var(--c-bg)', borderTop: '2px solid var(--c-b1)' }}>
              <div className="px-4 py-2 text-xs font-bold uppercase tracking-widest"
                style={{ color: 'var(--c-purple)', letterSpacing: '0.06em', backgroundColor: 'var(--c-s2)', borderBottom: '1px solid var(--c-b1)' }}>
                Valores Calculados — Vista Previa
              </div>
              <div className="p-4 grid gap-3" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                {[
                  { col: 'F', label: 'Gen. Bruta', unit: 'kWh', formula: '(EPSA_actual − EPSA_ant) × 2.400', value: previewCalc.genBrutaKwh, color: 'var(--c-blue-l)' },
                  { col: 'O', label: 'KWH G-1',    unit: 'kWh', formula: '(Cont.G1_actual − ant.) × 1.363,63', value: previewCalc.kwhG1, color: 'var(--c-green)' },
                  { col: 'AM', label: 'KWH G-2',   unit: 'kWh', formula: '(Cont.G2_actual − ant.) × 1.363,63', value: previewCalc.kwhG2, color: 'var(--c-purple)' },
                ].map(({ col, label, unit, formula, value, color }) => (
                  <div key={col} className="rounded-lg p-3"
                    style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b1)' }}>
                    <div className="flex items-center gap-1.5 mb-1">
                      <span className="text-xs font-mono font-bold" style={{ color: 'var(--c-t5)' }}>{col}</span>
                      <span className="text-xs font-semibold" style={{ color: 'var(--c-t2)' }}>{label}</span>
                      <span className="text-xs" style={{ color: 'var(--c-t5)' }}>({unit})</span>
                    </div>
                    <div className="font-mono font-bold text-base" style={{ color: value ? color : 'var(--c-t5)' }}>
                      {value || '—'}
                    </div>
                    <div className="text-xs mt-1 font-mono" style={{ color: 'var(--c-t5)', fontSize: 10 }}>{formula}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Observations */}
          <div className="rounded-lg p-4 mt-4" style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b1)' }}>
            <label className="block text-xs font-semibold uppercase tracking-widest mb-2" style={{ color: 'var(--c-t3)' }}>
              Observaciones del Turno
            </label>
            <textarea
              value={observations}
              disabled={!isOnDuty}
              onChange={e => setObservations(e.target.value)}
              rows={3}
              placeholder="Novedades, eventos o situaciones especiales durante el turno..."
              className={`w-full text-xs rounded-md px-3 py-2 resize-none ${!isOnDuty ? 'opacity-60 cursor-not-allowed' : ''}`}
              style={{ backgroundColor: 'var(--c-bg)', border: '1px solid var(--c-b1)', color: 'var(--c-t1)', outline: 'none', fontFamily: 'var(--font-sans)' }}
              onFocus={e => { if (isOnDuty) e.target.style.borderColor = 'var(--c-blue)'; }}
              onBlur={e =>  (e.target.style.borderColor = 'var(--c-b1)')}
            />
          </div>
        </form>
      </div>
    </div>
  );

  // ── Grid tab ─────────────────────────────────────────────────────────────────

  const gridTab = (
    <div className="flex-1 flex flex-col overflow-hidden">
      <div className="px-5 py-2 flex items-center gap-2 shrink-0"
        style={{ backgroundColor: 'var(--c-bg)', borderBottom: '1px solid var(--c-b1)' }}>
        <span className="text-xs" style={{ color: 'var(--c-t4)' }}>
          Cuadrícula 24 horas — fecha: <span className="font-mono text-[var(--c-t2)] font-semibold">{selectedDate}</span>.
        </span>
        <span className="ml-auto text-xs font-mono" style={{ color: 'var(--c-t4)' }}>
          {COLUMN_DEFS.length + 1} columnas · 24 filas
        </span>
      </div>

      <div style={{ overflowX: 'auto', overflowY: 'auto', flex: 1 }}>
        <table style={{ borderCollapse: 'collapse', tableLayout: 'fixed',
          minWidth: COLUMN_DEFS.reduce((s, c) => s + c.width, 60) + 'px' }}>
          <colgroup>
            <col style={{ width: 60 }} />
            {COLUMN_DEFS.map(c => <col key={c.key} style={{ width: c.width }} />)}
          </colgroup>

          <thead style={{ position: 'sticky', top: 0, zIndex: 4 }}>
            {/* Group header row */}
            <tr>
              <th rowSpan={2} style={{
                position: 'sticky', left: 0, zIndex: 5,
                backgroundColor: 'var(--c-s2)', border: '1px solid var(--c-b1)',
                fontSize: 9, fontWeight: 700, color: 'var(--c-t4)',
                textAlign: 'center', textTransform: 'uppercase', letterSpacing: '0.06em', padding: 4,
              }}>Hora</th>
              {Object.entries(
                COLUMN_DEFS.reduce((acc, col) => {
                  acc[col.group] = (acc[col.group] || 0) + 1;
                  return acc;
                }, {} as Record<string, number>)
              ).map(([group, span]) => (
                <th key={group} colSpan={span} style={{
                  backgroundColor: 'var(--c-s2)',
                  border: '1px solid var(--c-b1)',
                  fontSize: 9, fontWeight: 700,
                  color: GROUP_META[group]?.text || 'var(--c-t3)',
                  textAlign: 'center', textTransform: 'uppercase',
                  letterSpacing: '0.05em', padding: '3px 6px', whiteSpace: 'nowrap',
                }}>
                  {group}
                </th>
              ))}
            </tr>

            {/* Column header row */}
            <tr>
              {COLUMN_DEFS.map(col => (
                <th key={col.key} style={{
                  backgroundColor: col.editable ? 'var(--c-s1)' : 'var(--c-bg)',
                  border: '1px solid var(--c-b1)',
                  fontSize: 9, fontWeight: 600, padding: '3px 5px',
                  color: col.editable ? 'var(--c-t3)' : 'var(--c-blue-l)',
                  textAlign: 'right', whiteSpace: 'nowrap',
                }}>
                  <div style={{ color: 'var(--c-t5)', fontFamily: 'var(--font-mono)', fontSize: 8 }}>{col.col}</div>
                  <div>{col.label}</div>
                  <div style={{ color: 'var(--c-t5)', fontWeight: 400 }}>{col.unit}</div>
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {readings.map((row, idx) => {
              const isCurrent = row.hour === DEMO_HOUR;
              const isEven    = idx % 2 === 0;
              const calc      = getCalcValues(readings, row.hour);

              return (
                <tr key={row.hour}
                  onClick={() => { if (isOnDuty) { loadHourIntoForm(row.hour); setTab('form'); } }}
                  style={{
                    backgroundColor: isCurrent
                      ? 'rgba(227,179,65,0.05)'
                      : isEven ? 'var(--c-bg)' : 'rgba(22,27,34,0.8)',
                    cursor: isOnDuty ? 'pointer' : 'default',
                    borderBottom: '1px solid var(--c-b3)',
                    opacity: !row.saved ? 0.35 : 1,
                  }}
                  className={isOnDuty ? "hover:bg-white/[0.03] transition-colors" : ""}>

                  <td style={{
                    position: 'sticky', left: 0, zIndex: 1,
                    backgroundColor: isCurrent ? 'rgba(227,179,65,0.08)' : isEven ? 'var(--c-bg)' : 'var(--c-s1)',
                    border: '1px solid var(--c-b1)',
                    padding: '0 6px', textAlign: 'center', height: 26,
                    fontFamily: 'var(--font-mono)', fontSize: 11, fontWeight: 700,
                    color: isCurrent ? 'var(--c-amber)' : row.saved ? 'var(--c-t3)' : 'var(--c-t5)',
                  }}>
                    {fmtHour(row.hour)}{isCurrent && ' ◀'}
                  </td>

                  {COLUMN_DEFS.map(col => {
                    const isCalc = !col.editable;
                    const val = isCalc
                      ? (col.key === 'genBrutaKwh' ? (calc.genBrutaKwh || row.genBrutaKwh)
                        : col.key === 'kwhG1'       ? (calc.kwhG1 || row.kwhG1)
                        : (calc.kwhG2 || row.kwhG2))
                      : row[col.key as keyof typeof row] as string;

                    return (
                      <td key={col.key} style={{
                        border: '1px solid rgba(48,54,61,0.6)',
                        backgroundColor: isCalc ? 'var(--c-bg)' : undefined,
                        padding: '0 5px', height: 26, textAlign: 'right',
                        fontFamily: 'var(--font-mono)', fontSize: 11,
                        color: isCalc ? 'var(--c-calc-txt)' : val ? 'var(--c-t2)' : 'var(--c-t5)',
                        fontWeight: isCalc ? 500 : 400,
                      }}>
                        {val || '—'}
                      </td>
                    );
                  })}
                </tr>
              );
            })}
          </tbody>

          <tfoot>
            <tr style={{ backgroundColor: 'var(--c-s2)', position: 'sticky', bottom: 0, zIndex: 2, borderTop: '2px solid var(--c-b2)' }}>
              <td style={{
                position: 'sticky', left: 0, zIndex: 3, backgroundColor: 'var(--c-s2)',
                border: '1px solid var(--c-b1)', padding: '4px 6px',
                fontSize: 9, fontWeight: 700, color: 'var(--c-t3)',
                textAlign: 'center', textTransform: 'uppercase',
              }}>TOTAL</td>

              {COLUMN_DEFS.map(col => {
                const isG1    = col.key === 'kwhG1';
                const isG2    = col.key === 'kwhG2';
                const isBruta = col.key === 'genBrutaKwh';
                const hasTotal = isG1 || isG2 || isBruta;
                const total = hasTotal
                  ? readings.reduce((s, r) => {
                      const v = getCalcValues(readings, r.hour);
                      const raw = isG1 ? (v.kwhG1 || r.kwhG1) : isG2 ? (v.kwhG2 || r.kwhG2) : (v.genBrutaKwh || r.genBrutaKwh);
                      return s + (parseInt((raw || '').replace(/\./g, '').replace(',', '.')) || 0);
                    }, 0)
                  : null;

                return (
                  <td key={col.key} style={{
                    border: '1px solid var(--c-b1)', padding: '4px 5px', textAlign: 'right',
                    fontFamily: 'var(--font-mono)', fontSize: 10, fontWeight: 700,
                    color: total ? 'var(--c-calc-txt)' : 'transparent',
                  }}>
                    {total ? total.toLocaleString('es-CO') : '—'}
                  </td>
                );
              })}
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );

  // ── Handoff modal ────────────────────────────────────────────────────────────

  const eligibleReceivers = operatorsList.filter(
    u => u.id !== currentUser?.id && u.usuario !== currentUser?.usuario && u.nombre.toLowerCase() !== userName.toLowerCase()
  );

  const handoffModal = showHandoff && (
    <div className="modal-backdrop fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ backgroundColor: 'rgba(13,17,23,0.85)', backdropFilter: 'blur(4px)' }}
      onClick={e => { if (e.target === e.currentTarget) { setShowHandoff(false); setHandoffDone(false); setHandoffTo(''); } }}>
      <div className="modal-card w-full max-w-sm rounded-xl"
        style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b2)', boxShadow: '0 24px 64px rgba(0,0,0,0.55)' }}>
        <div className="px-5 py-4" style={{ borderBottom: '1px solid var(--c-b1)' }}>
          <h2 className="text-sm font-semibold" style={{ color: 'var(--c-t1)' }}>Entrega de Turno</h2>
          <p className="text-xs mt-0.5" style={{ color: 'var(--c-t4)' }}>Relevo de guardia y transferencia de permisos</p>
        </div>
        {handoffDone ? (
          <div className="px-5 py-8 flex flex-col items-center gap-3">
            <div className="w-10 h-10 rounded-full flex items-center justify-center"
              style={{ backgroundColor: 'var(--c-green-bg)', border: '1px solid rgba(63,185,80,0.3)' }}>
              <svg viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5" style={{ color: 'var(--c-green)' }}>
                <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd"/>
              </svg>
            </div>
            <p style={{ fontSize: 13, fontWeight: 600, color: 'var(--c-green)' }}>Turno entregado con éxito</p>
            <p style={{ fontSize: 12, color: 'var(--c-t4)' }}>Recibido por <strong style={{ color: 'var(--c-t1)' }}>{handoffTo}</strong></p>
            <p className="text-xs text-center text-[var(--c-t4)]">
              La potestad para registrar y modificar lecturas horarias ha sido transferida a {handoffTo}.
            </p>
            <button onClick={() => { setShowHandoff(false); setHandoffDone(false); setHandoffTo(''); }}
              className="mt-2 px-5 py-2 text-xs font-semibold rounded-md transition-colors"
              style={{ backgroundColor: 'var(--c-blue)', color: '#fff', border: 'none', cursor: 'pointer' }}>
              Entendido
            </button>
          </div>
        ) : (
          <div className="px-5 py-4 space-y-4">
            <div>
              <label style={{ display: 'block', fontSize: 10, fontWeight: 700, color: 'var(--c-t3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>
                Operador que recibe el turno
              </label>
              <select value={handoffTo} onChange={e => setHandoffTo(e.target.value)}
                className="w-full text-xs rounded-md px-3 py-2"
                style={{ backgroundColor: 'var(--c-bg)', border: '1px solid var(--c-b1)', color: 'var(--c-t1)', outline: 'none', cursor: 'pointer' }}>
                <option value="">Seleccionar operador entrante...</option>
                {eligibleReceivers.map(u => (
                  <option key={u.id} value={u.nombre}>{u.nombre}</option>
                ))}
              </select>
            </div>
            <div className="flex gap-3">
              <button onClick={() => { setShowHandoff(false); setHandoffDone(false); setHandoffTo(''); }}
                className="flex-1 py-2 text-xs font-semibold rounded-md"
                style={{ backgroundColor: 'var(--c-s2)', color: 'var(--c-t3)', border: '1px solid var(--c-b1)', cursor: 'pointer' }}>
                Cancelar
              </button>
              <button disabled={!handoffTo}
                onClick={async () => {
                  const receivingOp = eligibleReceivers.find(o => o.nombre === handoffTo);
                  if (receivingOp) {
                    try {
                      await apiRecordHandoff({
                        date: selectedDate,
                        hour: formHour,
                        receivingUserId: receivingOp.id,
                        notes: `Turno entregado por ${userName} a ${handoffTo}`,
                      });
                      const updatedShift = await apiGetCurrentShift();
                      setCurrentShiftInfo(updatedShift);
                      setHandoffDone(true);
                    } catch (err: any) {
                      setApiError(err.message || 'Error al registrar entrega de turno');
                    }
                  }
                }}
                className="flex-1 py-2 text-xs font-bold rounded-md"
                style={{
                  backgroundColor: handoffTo ? 'var(--c-green-bg)' : 'var(--c-s2)',
                  color: handoffTo ? 'var(--c-green)' : 'var(--c-t5)',
                  border: `1px solid ${handoffTo ? 'rgba(63,185,80,0.3)' : 'var(--c-b1)'}`,
                  cursor: handoffTo ? 'pointer' : 'not-allowed',
                }}>
                Confirmar Entrega
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );

  // ── Render ───────────────────────────────────────────────────────────────────

  return (
    <div className="min-h-screen flex flex-col" style={{ backgroundColor: 'var(--c-bg)' }}>
      {navbar}
      {shiftBar}
      {tabBar}
      {tab === 'form' ? formTab : gridTab}
      {pending && (
        <JustifyModal
          pending={pending}
          onConfirm={j => applyFormValues(j)}
          onCancel={() => setPending(null)}
        />
      )}
      {handoffModal}
    </div>
  );
}
