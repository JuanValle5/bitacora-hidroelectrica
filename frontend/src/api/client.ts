import type { AppUser, AuditEntry, DailyReportData, DashboardMetrics, HourlyReading, ShiftInfo } from '../types';

const API_BASE = (import.meta as any).env?.VITE_API_URL || 'http://localhost:8081/api/v1';

function getToken(): string | null {
  return localStorage.getItem('bitacora_token');
}

export function setToken(token: string | null) {
  if (token) {
    localStorage.setItem('bitacora_token', token);
  } else {
    localStorage.removeItem('bitacora_token');
  }
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (!res.ok) {
    let errorMsg = 'Error en el servidor';
    try {
      const errJson = await res.json();
      errorMsg = errJson.message || errJson.error || errorMsg;
    } catch {
      errorMsg = `Error HTTP ${res.status}: ${res.statusText}`;
    }
    throw new Error(errorMsg);
  }

  if (res.status === 204) {
    return {} as T;
  }

  return res.json();
}

// ── Auth ─────────────────────────────────────────────────────────────
export async function apiLogin(username: string, password: string): Promise<{ token: string; user: AppUser }> {
  const data = await request<{
    token: string;
    tokenType: string;
    expiresIn: number;
    user: AppUser;
  }>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });

  setToken(data.token);
  return { token: data.token, user: data.user };
}

export async function apiGetCurrentUser(): Promise<AppUser> {
  return request<AppUser>('/auth/me');
}

export function apiLogout() {
  setToken(null);
}

// ── Daily Reports & Readings ─────────────────────────────────────────
export async function apiGetDailyReport(date: string): Promise<DailyReportData> {
  return request<DailyReportData>(`/daily-reports?date=${date}`);
}

export function parseNumber(val: unknown): number | null {
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

export async function apiRegisterReading(date: string, reading: Record<string, any>): Promise<HourlyReading> {
  // Convert string values to numbers where applicable
  const payload: Record<string, any> = { ...reading };
  for (const [key, val] of Object.entries(payload)) {
    if (key !== 'observations' && val !== null && val !== undefined && val !== '') {
      const parsed = parseNumber(val);
      if (parsed !== null) {
        payload[key] = parsed;
      }
    }
  }

  return request<HourlyReading>(`/daily-reports/${date}/readings`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function apiUpdateReading(
  date: string,
  hour: number,
  reading: Record<string, any>,
  justification: string
): Promise<HourlyReading> {
  const payload: Record<string, any> = {
    ...reading,
    justification,
  };

  for (const [key, val] of Object.entries(payload)) {
    if (key !== 'observations' && key !== 'justification' && val !== null && val !== undefined && val !== '') {
      const parsed = parseNumber(val);
      if (parsed !== null) {
        payload[key] = parsed;
      }
    }
  }

  return request<HourlyReading>(`/daily-reports/${date}/readings/${hour}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export async function apiExportPdf(date: string): Promise<void> {
  const token = getToken();
  const headers: Record<string, string> = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}/daily-reports/${date}/export/pdf`, {
    headers,
  });

  if (!res.ok) {
    throw new Error('Error al generar y descargar el reporte PDF');
  }

  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `bitacora-hidroelectrica-${date}.pdf`;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
}

// ── Users ────────────────────────────────────────────────────────────
export async function apiGetUsers(): Promise<AppUser[]> {
  return request<AppUser[]>('/users');
}

export async function apiCreateUser(data: {
  nombre: string;
  usuario: string;
  password: string;
  rol: string;
  turno?: string;
}): Promise<AppUser> {
  return request<AppUser>('/users', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function apiToggleUserStatus(id: number): Promise<AppUser> {
  return request<AppUser>(`/users/${id}/toggle-status`, {
    method: 'PATCH',
  });
}

// ── Audit Logs ───────────────────────────────────────────────────────
export async function apiGetAuditLogs(date?: string, search?: string): Promise<AuditEntry[]> {
  const params = new URLSearchParams();
  if (date) params.append('date', date);
  if (search) params.append('search', search);

  const qs = params.toString() ? `?${params.toString()}` : '';
  return request<AuditEntry[]>(`/audit-logs${qs}`);
}

// ── Dashboard & Shifts ───────────────────────────────────────────────
export async function apiGetDashboardMetrics(date: string): Promise<DashboardMetrics> {
  return request<DashboardMetrics>(`/dashboard/metrics?date=${date}`);
}

export async function apiGetCurrentShift(): Promise<ShiftInfo> {
  return request<ShiftInfo>('/shifts/current');
}

export async function apiRecordHandoff(data: {
  date: string;
  hour: number;
  receivingUserId: number;
  shiftType?: string;
  notes?: string;
}): Promise<any> {
  return request('/shifts/handoff', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
