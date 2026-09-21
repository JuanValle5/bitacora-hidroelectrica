import { useState, useEffect } from 'react';
import type { AuditEntry, AppUser, HourlyReading, DashboardMetrics } from '../types';
import {
  generateInitialReadings,
  getCalcValues,
  fmtHour,
  COLUMN_DEFS,
  GROUP_META,
} from '../data/mockData';
import {
  apiGetUsers,
  apiCreateUser,
  apiToggleUserStatus,
  apiGetAuditLogs,
  apiExportPdf,
  apiGetDashboardMetrics,
  apiGetDailyReport,
} from '../api/client';
import UserModal from '../components/UserModal';

interface Props {
  userName: string;
  onLogout: () => void;
  auditLog?: AuditEntry[];
}

function MetricCard({ label, value, sub, accent = 'var(--c-blue)', icon }: {
  label: string; value: string; sub: string; accent?: string; icon: React.ReactNode;
}) {
  return (
    <div className="rounded-lg p-5 flex flex-col gap-4"
      style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b1)' }}>
      <div className="flex items-start justify-between">
        <span className="text-xs font-semibold uppercase tracking-widest" style={{ color: 'var(--c-t3)' }}>{label}</span>
        <div className="w-7 h-7 rounded-md flex items-center justify-center"
          style={{ backgroundColor: `color-mix(in srgb, ${accent} 14%, transparent)`, border: `1px solid color-mix(in srgb, ${accent} 35%, transparent)` }}>
          <span style={{ color: accent }}>{icon}</span>
        </div>
      </div>
      <div>
        <div className="text-2xl font-bold font-mono leading-none" style={{ color: 'var(--c-t1)', letterSpacing: '-0.02em' }}>{value}</div>
        <div className="text-xs mt-1.5" style={{ color: 'var(--c-t4)' }}>{sub}</div>
      </div>
    </div>
  );
}

function NavBar({
  userName,
  onLogout,
  selectedDate,
  onDateChange,
}: {
  userName: string;
  onLogout: () => void;
  selectedDate: string;
  onDateChange: (date: string) => void;
}) {
  return (
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
        <span className="px-2 py-0.5 rounded text-xs font-semibold font-mono"
          style={{ backgroundColor: 'rgba(227,179,65,0.12)', color: 'var(--c-amber)', border: '1px solid rgba(227,179,65,0.25)' }}>
          Administrador
        </span>
      </div>
      <div className="flex items-center gap-4">
        <input
          type="date"
          value={selectedDate}
          onChange={e => onDateChange(e.target.value)}
          className="text-xs font-mono rounded px-2 py-1"
          style={{ backgroundColor: 'var(--c-s2)', border: '1px solid var(--c-b1)', color: 'var(--c-t2)', outline: 'none', cursor: 'pointer' }}
        />
        <span className="text-xs font-mono" style={{ color: 'var(--c-t4)' }}>
          {new Date().toLocaleDateString('es-CO', { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' })}
        </span>
        <span className="text-xs" style={{ color: 'var(--c-t3)' }}>{userName}</span>
        <button onClick={onLogout}
          className="text-xs px-3 py-1 rounded-md font-semibold transition-colors"
          style={{ backgroundColor: 'var(--c-s2)', color: 'var(--c-t3)', border: '1px solid var(--c-b1)', cursor: 'pointer' }}
          onMouseEnter={e => (e.currentTarget.style.color = 'var(--c-red)')}
          onMouseLeave={e => (e.currentTarget.style.color = 'var(--c-t3)')}>
          Cerrar Sesión
        </button>
      </div>
    </header>
  );
}

export default function AdminView({ userName, onLogout, auditLog = [] }: Props) {
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [users, setUsers] = useState<AppUser[]>([]);
  const [showUsers, setShowUsers] = useState(false);
  const [auditFilter, setAuditFilter] = useState('');
  const [activeTab, setActiveTab] = useState<'overview' | 'grid'>('overview');
  const [readings, setReadings] = useState<HourlyReading[]>(generateInitialReadings);
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [auditList, setAuditList] = useState<AuditEntry[]>(auditLog);
  const [exporting, setExporting] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadAdminData() {
      try {
        const [reportData, metricsData, auditData, usersData] = await Promise.all([
          apiGetDailyReport(selectedDate).catch(err => {
            console.warn('Daily report load error:', err.message);
            return null;
          }),
          apiGetDashboardMetrics(selectedDate).catch(err => {
            console.warn('Dashboard metrics error:', err.message);
            return null;
          }),
          apiGetAuditLogs(selectedDate).catch(err => {
            console.warn('Audit logs load error:', err.message);
            return null;
          }),
          apiGetUsers().catch(err => {
            console.warn('Users load error:', err.message);
            return null;
          }),
        ]);

        if (!isMounted) return;

        if (reportData && reportData.readings && reportData.readings.length > 0) {
          setReadings(reportData.readings);
        } else {
          setReadings(generateInitialReadings());
        }

        if (metricsData) {
          setMetrics(metricsData);
        }

        if (auditData) {
          setAuditList(auditData);
        }

        if (usersData) {
          setUsers(usersData);
        }
      } catch (err: any) {
        if (isMounted) {
          setActionError(err.message || 'Error cargando datos del sistema');
        }
      }
    }

    loadAdminData();

    return () => {
      isMounted = false;
    };
  }, [selectedDate]);

  const isToday = selectedDate === new Date().toISOString().split('T')[0];
  const currentHour = isToday ? new Date().getHours() : -1;

  const pastReadings = readings.filter(r => r.saved);
  const calculatedTotalKwh = readings.reduce((acc, r) => {
    const c = getCalcValues(readings, r.hour);
    const g1 = parseInt((r.kwhG1 || c.kwhG1 || '').replace(/\./g, '').replace(',', '.')) || 0;
    const g2 = parseInt((r.kwhG2 || c.kwhG2 || '').replace(/\./g, '').replace(',', '.')) || 0;
    return acc + g1 + g2;
  }, 0);

  const totalKwh = metrics ? metrics.totalGenerationKwh : calculatedTotalKwh;
  const totalMWh = metrics ? metrics.totalGenerationMwh : totalKwh / 1000;
  const avgMW = metrics ? metrics.averagePowerMw : (pastReadings.length > 0 ? totalMWh / pastReadings.length : 0);
  const shiftLabel = metrics?.activeShiftLabel || (new Date().getHours() < 18 ? 'Turno A' : 'Turno B');
  const shiftSub = metrics?.activeShiftRange || '06:00–18:00';

  const filteredAudit = auditList.filter(e =>
    !auditFilter ||
    (e.operador && e.operador.toLowerCase().includes(auditFilter.toLowerCase())) ||
    (e.campo && e.campo.toLowerCase().includes(auditFilter.toLowerCase())) ||
    (e.justificacion && e.justificacion.toLowerCase().includes(auditFilter.toLowerCase()))
  );

  const handleExportPdf = async () => {
    try {
      setExporting(true);
      setActionError(null);
      await apiExportPdf(selectedDate);
      setActionSuccess(`Reporte PDF del ${selectedDate} descargado con éxito.`);
      setTimeout(() => setActionSuccess(null), 4000);
    } catch (err: any) {
      setActionError(err.message || 'Error al exportar el reporte PDF.');
    } finally {
      setExporting(false);
    }
  };

  const handleAddUser = async (u: Omit<AppUser, 'id'> & { password?: string }) => {
    try {
      setActionError(null);
      const created = await apiCreateUser({
        nombre: u.nombre,
        usuario: u.usuario,
        password: u.password || 'TempPassword123!',
        rol: u.rol,
        turno: u.turno,
      });
      setUsers(prev => [...prev, created]);
      setActionSuccess(`Usuario ${created.usuario} creado exitosamente.`);
      setTimeout(() => setActionSuccess(null), 4000);
    } catch (err: any) {
      setActionError(err.message || 'Error al crear usuario.');
    }
  };

  const handleToggleUser = async (id: number) => {
    try {
      setActionError(null);
      const updated = await apiToggleUserStatus(id);
      setUsers(prev => prev.map(u => u.id === id ? updated : u));
      setActionSuccess(`Usuario ${updated.usuario} ${updated.activo ? 'activado' : 'desactivado'}.`);
      setTimeout(() => setActionSuccess(null), 4000);
    } catch (err: any) {
      setActionError(err.message || 'Error al actualizar el estado del usuario.');
    }
  };

  const btn = (label: string, icon: React.ReactNode, primary = false, onClick?: () => void, disabled = false) => (
    <button onClick={onClick} disabled={disabled}
      className="flex items-center gap-2 px-4 py-2 rounded-md text-xs font-semibold transition-colors disabled:opacity-50"
      style={{
        backgroundColor: primary ? 'var(--c-blue)' : 'var(--c-s2)',
        color: primary ? '#fff' : 'var(--c-t3)',
        border: primary ? 'none' : '1px solid var(--c-b1)',
        cursor: disabled ? 'not-allowed' : 'pointer',
      }}
      onMouseEnter={e => { if (!disabled) e.currentTarget.style.backgroundColor = primary ? 'var(--c-blue-h)' : 'var(--c-s3)'; }}
      onMouseLeave={e => { if (!disabled) e.currentTarget.style.backgroundColor = primary ? 'var(--c-blue)' : 'var(--c-s2)'; }}>
      {icon}{label}
    </button>
  );

  const fullGrid = (
    <div className="flex-1 flex flex-col overflow-hidden">
      <div className="px-5 py-2 flex items-center gap-2 shrink-0"
        style={{ backgroundColor: 'var(--c-bg)', borderBottom: '1px solid var(--c-b1)' }}>
        <span className="text-xs" style={{ color: 'var(--c-t4)' }}>
          Vista de solo lectura — fecha: <span className="font-mono text-[var(--c-t2)] font-semibold">{selectedDate}</span>.
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
              const isCurrent = isToday && row.hour === currentHour;
              const isEven    = idx % 2 === 0;
              const calc      = getCalcValues(readings, row.hour);
              return (
                <tr key={row.hour} style={{
                  backgroundColor: isCurrent
                    ? 'rgba(227,179,65,0.05)'
                    : isEven ? 'var(--c-bg)' : 'rgba(22,27,34,0.8)',
                  borderBottom: '1px solid var(--c-b3)',
                  opacity: !row.saved ? 0.35 : 1,
                }}>
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

  return (
    <div className="min-h-screen flex flex-col" style={{ backgroundColor: 'var(--c-bg)' }}>
      <NavBar
        userName={userName}
        onLogout={onLogout}
        selectedDate={selectedDate}
        onDateChange={setSelectedDate}
      />

      {/* Tab bar */}
      <div className="flex shrink-0" style={{ backgroundColor: 'var(--c-s1)', borderBottom: '1px solid var(--c-b1)' }}>
        {([
          { key: 'overview' as const, label: 'Panel General' },
          { key: 'grid'     as const, label: 'Cuadrícula de Registro' },
        ]).map(t => (
          <button key={t.key} onClick={() => setActiveTab(t.key)}
            className="px-5 py-2.5 text-xs font-semibold"
            style={{
              color: activeTab === t.key ? 'var(--c-blue)' : 'var(--c-t4)',
              borderBottom: activeTab === t.key ? '2px solid var(--c-blue)' : '2px solid transparent',
              marginBottom: -1, background: 'none',
              borderTop: 'none', borderLeft: 'none', borderRight: 'none',
              cursor: 'pointer',
            }}>
            {t.label}
          </button>
        ))}
      </div>

      {/* Notification Banners */}
      {actionError && (
        <div className="px-5 py-2.5 bg-red-950/50 border-b border-red-800/60 flex items-center justify-between text-xs text-red-300">
          <div className="flex items-center gap-2">
            <span className="font-bold">Error:</span> {actionError}
          </div>
          <button onClick={() => setActionError(null)} className="text-red-400 hover:text-red-200 cursor-pointer text-sm">×</button>
        </div>
      )}
      {actionSuccess && (
        <div className="px-5 py-2.5 bg-emerald-950/50 border-b border-emerald-800/60 flex items-center justify-between text-xs text-emerald-300">
          <div className="flex items-center gap-2">
            <span className="font-bold">Éxito:</span> {actionSuccess}
          </div>
          <button onClick={() => setActionSuccess(null)} className="text-emerald-400 hover:text-emerald-200 cursor-pointer text-sm">×</button>
        </div>
      )}

      {activeTab === 'grid' ? fullGrid : (
      <main className="flex-1 p-5 space-y-5 overflow-auto">
        {/* Metrics */}
        <div className="grid gap-4" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
          <MetricCard
            label="Generación Total Hoy"
            value={`${totalMWh.toFixed(1)} MWh`}
            sub={`${metrics?.completedHours ?? pastReadings.length} / 24 h registradas ${metrics ? `(${metrics.completionPercentage}%)` : ''}`}
            accent="var(--c-blue)"
            icon={<svg viewBox="0 0 16 16" fill="currentColor" className="w-3.5 h-3.5"><path d="M8 1L1 9h5.5L5 15l9-8H8.5L10 1z"/></svg>}
          />
          <MetricCard
            label="Potencia Promedio"
            value={`${avgMW.toFixed(2)} MW`}
            sub="Media del día en curso"
            accent="var(--c-green)"
            icon={<svg viewBox="0 0 16 16" fill="currentColor" className="w-3.5 h-3.5"><path d="M2 11l4-8 3 5 2-3 3 6H2z"/></svg>}
          />
          <MetricCard
            label="Turno en Curso"
            value={shiftLabel}
            sub={shiftSub}
            accent="var(--c-amber)"
            icon={<svg viewBox="0 0 16 16" fill="currentColor" className="w-3.5 h-3.5"><path fillRule="evenodd" d="M8 1a7 7 0 100 14A7 7 0 008 1zM8 3a.75.75 0 01.75.75v3.69l2.28 2.28a.75.75 0 11-1.06 1.06L7.47 8.28A.75.75 0 017.25 7.5v-3.75A.75.75 0 018 3z" clipRule="evenodd"/></svg>}
          />
          <MetricCard
            label="Registros de Auditoría"
            value={String(auditList.length)}
            sub="Modificaciones del período"
            accent="var(--c-purple)"
            icon={<svg viewBox="0 0 16 16" fill="currentColor" className="w-3.5 h-3.5"><path d="M11.013 2.5a1.5 1.5 0 012.987.25v.25l-7.5 7.5-.75 3 3-.75 7.5-7.5V5a1.5 1.5 0 01-2.987-.25l-2.25-2.25z"/></svg>}
          />
        </div>

        {/* Action bar */}
        <div className="flex items-center gap-3">
          {btn('Gestión de Usuarios',
            <svg viewBox="0 0 16 16" fill="currentColor" className="w-3.5 h-3.5"><path d="M8.5 4.5a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0zM10.9 12.006C10.8 9.61 9.04 8 7 8s-3.8 1.61-3.9 4.006A.75.75 0 003.85 13H10.15a.75.75 0 00.75-.994zM15 5a.75.75 0 01.75.75v1.5h1.5a.75.75 0 010 1.5h-1.5v1.5a.75.75 0 01-1.5 0V8.75H12.75a.75.75 0 010-1.5h1.5V5.75A.75.75 0 0115 5z"/></svg>,
            true, () => setShowUsers(true))}
          {btn(exporting ? 'Generando PDF...' : 'Exportar Reporte PDF',
            <svg viewBox="0 0 16 16" fill="currentColor" className="w-3.5 h-3.5"><path d="M7.25 10.25a.75.75 0 001.5 0V4.56l2.22 2.22a.75.75 0 101.06-1.06l-3.5-3.5a.75.75 0 00-1.06 0l-3.5 3.5a.75.75 0 001.06 1.06l2.22-2.22v5.69zM3.5 13.25a.75.75 0 000 1.5h9a.75.75 0 000-1.5h-9z"/></svg>,
            false, handleExportPdf, exporting)}
        </div>

        {/* Two-column layout */}
        <div className="grid gap-5" style={{ gridTemplateColumns: '1fr 400px' }}>

          {/* Monitoring table */}
          <div className="rounded-lg overflow-hidden flex flex-col"
            style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b1)' }}>
            <div className="flex items-center justify-between px-4 py-3"
              style={{ borderBottom: '1px solid var(--c-b1)' }}>
              <div>
                <h2 className="text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--c-t1)' }}>
                  Monitoreo 24 h — Solo Lectura
                </h2>
                <p className="text-xs mt-0.5" style={{ color: 'var(--c-t4)' }}>
                  Fecha: <span className="font-mono text-[var(--c-t2)] font-semibold">{selectedDate}</span>
                </p>
              </div>
              <div className="flex items-center gap-1.5 text-xs" style={{ color: 'var(--c-green)' }}>
                <span className="w-1.5 h-1.5 rounded-full pulse-dot" style={{ backgroundColor: 'var(--c-green)' }} />
                En vivo
              </div>
            </div>
            <div style={{ overflowX: 'auto', maxHeight: 400, overflowY: 'auto', flex: 1 }}>
              <table style={{ borderCollapse: 'collapse', width: '100%', minWidth: 680 }}>
                <thead style={{ position: 'sticky', top: 0, zIndex: 1, backgroundColor: 'var(--c-s1)' }}>
                  <tr style={{ borderBottom: '1px solid var(--c-b1)' }}>
                    {['Hora', 'Niv. Carga (msnm)', 'Niv. Desc. (msnm)', 'Pot. G-1 (kW)', 'Pot. G-2 (kW)', 'EPSA Actaris', 'Gen. Bruta (kWh)', 'KWH G-1', 'KWH G-2'].map(h => (
                      <th key={h} style={{ padding: '6px 12px', fontSize: 10, fontWeight: 700, color: 'var(--c-t3)', textAlign: 'right', whiteSpace: 'nowrap', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {readings.map((r, idx) => {
                    const isCurrent = isToday && r.hour === currentHour;
                    const isEven    = idx % 2 === 0;
                    const calc      = getCalcValues(readings, r.hour);
                    const genBruta  = calc.genBrutaKwh || r.genBrutaKwh;
                    const kwh1      = calc.kwhG1 || r.kwhG1;
                    const kwh2      = calc.kwhG2 || r.kwhG2;

                    return (
                      <tr key={r.hour} style={{
                        borderBottom: '1px solid var(--c-b3)',
                        backgroundColor: isCurrent ? 'rgba(227,179,65,0.05)' : isEven ? 'transparent' : 'rgba(255,255,255,0.01)',
                        opacity: !r.saved ? 0.35 : 1,
                      }}>
                        <td style={{ padding: '5px 12px', fontFamily: 'var(--font-mono)', fontSize: 11, fontWeight: 700, color: isCurrent ? 'var(--c-amber)' : 'var(--c-t3)', whiteSpace: 'nowrap' }}>
                          {fmtHour(r.hour)}{isCurrent && ' ◀'}
                        </td>
                        {[r.nivelCarga, r.nivelDescarga, r.potActivaG1, r.potActivaG2, r.epsaActarisKwh, genBruta, kwh1, kwh2].map((v, i) => (
                          <td key={i} style={{ padding: '5px 12px', fontFamily: 'var(--font-mono)', fontSize: 11, textAlign: 'right', color: i >= 5 ? 'var(--c-calc-txt)' : 'var(--c-t2)' }}>
                            {v || <span style={{ color: 'var(--c-t5)' }}>—</span>}
                          </td>
                        ))}
                      </tr>
                    );
                  })}
                </tbody>
                <tfoot>
                  <tr style={{ backgroundColor: 'var(--c-s2)', borderTop: '1px solid var(--c-b1)', position: 'sticky', bottom: 0 }}>
                    <td colSpan={6} style={{ padding: '5px 12px', fontSize: 10, fontWeight: 700, color: 'var(--c-t3)', textTransform: 'uppercase' }}>TOTAL DÍA</td>
                    <td style={{ padding: '5px 12px', fontFamily: 'var(--font-mono)', fontSize: 11, fontWeight: 700, textAlign: 'right', color: 'var(--c-calc-txt)' }}>
                      {totalKwh.toLocaleString('es-CO')} kWh
                    </td>
                    <td colSpan={2} />
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>

          {/* Audit log */}
          <div className="rounded-lg overflow-hidden flex flex-col"
            style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b1)' }}>
            <div className="px-4 py-3" style={{ borderBottom: '1px solid var(--c-b1)' }}>
              <h2 className="text-xs font-bold uppercase tracking-widest" style={{ color: 'var(--c-t1)' }}>Trazabilidad — Auditoría</h2>
              <input type="text" value={auditFilter} onChange={e => setAuditFilter(e.target.value)}
                placeholder="Filtrar por operador, campo o justificación..."
                className="mt-2 w-full text-xs rounded-md px-2.5 py-1.5"
                style={{ backgroundColor: 'var(--c-bg)', border: '1px solid var(--c-b1)', color: 'var(--c-t1)', outline: 'none' }} />
            </div>
            <div style={{ overflowY: 'auto', flex: 1, maxHeight: 400 }}>
              {filteredAudit.length === 0 ? (
                <p className="text-xs text-center py-8" style={{ color: 'var(--c-t5)' }}>Sin registros de auditoría.</p>
              ) : filteredAudit.map(entry => (
                <div key={entry.id} className="px-4 py-3" style={{ borderBottom: '1px solid var(--c-b3)' }}>
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-semibold" style={{ color: 'var(--c-t1)' }}>{entry.campo}</span>
                    <span className="text-xs font-mono" style={{ color: 'var(--c-t4)' }}>{entry.timestamp}</span>
                  </div>
                  <div className="flex items-center gap-2 mb-1.5">
                    <span className="text-xs font-mono px-1.5 py-0.5 rounded"
                      style={{ backgroundColor: 'var(--c-red-bg)', color: 'var(--c-red)', border: '1px solid rgba(248,81,73,0.2)' }}>
                      {entry.valorAnterior}
                    </span>
                    <svg viewBox="0 0 12 12" fill="currentColor" className="w-3 h-3 shrink-0" style={{ color: 'var(--c-t4)' }}>
                      <path d="M4.5 1.5L7.5 6l-3 4.5" strokeWidth="1.5" stroke="currentColor" fill="none" strokeLinecap="round" />
                    </svg>
                    <span className="text-xs font-mono px-1.5 py-0.5 rounded"
                      style={{ backgroundColor: 'var(--c-green-bg)', color: 'var(--c-green)', border: '1px solid rgba(63,185,80,0.2)' }}>
                      {entry.valorNuevo}
                    </span>
                    <span className="text-xs ml-auto font-mono" style={{ color: 'var(--c-t4)' }}>h {entry.hora}</span>
                  </div>
                  <div className="flex items-start gap-1.5">
                    <span className="text-xs" style={{ color: 'var(--c-t4)' }}>por</span>
                    <span className="text-xs font-semibold" style={{ color: 'var(--c-blue-l)' }}>{entry.operador}</span>
                    <span className="text-xs ml-1 italic" style={{ color: 'var(--c-t4)' }}>"{entry.justificacion}"</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>
      )}

      {showUsers && (
        <UserModal
          users={users}
          onClose={() => setShowUsers(false)}
          onAdd={handleAddUser}
          onToggle={handleToggleUser}
        />
      )}
    </div>
  );
}
