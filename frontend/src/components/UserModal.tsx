import { useState } from 'react';
import type { AppUser } from '../types';

interface Props {
  users: AppUser[];
  onClose: () => void;
  onAdd: (user: Omit<AppUser, 'id'> & { password?: string }) => void;
  onToggle: (id: number) => void;
}

export default function UserModal({ users, onClose, onAdd, onToggle }: Props) {
  const [tab, setTab] = useState<'list' | 'new'>('list');
  const [nombre, setNombre] = useState('');
  const [usuario, setUsuario] = useState('');
  const [password, setPassword] = useState('');
  const [rol, setRol] = useState<'admin' | 'operador'>('operador');
  const [turno, setTurno] = useState('');
  const [success, setSuccess] = useState(false);

  const inp: React.CSSProperties = {
    width: '100%',
    backgroundColor: 'var(--c-bg)',
    border: '1px solid var(--c-b1)',
    borderRadius: 6,
    color: 'var(--c-t1)',
    fontSize: 12,
    padding: '7px 10px',
    outline: 'none',
  };

  const handleAdd = (e: React.FormEvent) => {
    e.preventDefault();
    onAdd({ nombre, usuario, rol, activo: true, turno, password: password || 'TempPassword123!' });
    setSuccess(true);
    setTimeout(() => { setSuccess(false); setNombre(''); setUsuario(''); setPassword(''); setTurno(''); setTab('list'); }, 1500);
  };

  return (
    <div className="modal-backdrop fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ backgroundColor: 'rgba(13,17,23,0.85)', backdropFilter: 'blur(4px)' }}
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}>

      <div className="modal-card w-full max-w-lg rounded-xl"
        style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b2)', boxShadow: '0 24px 64px rgba(0,0,0,0.55)' }}>

        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4"
          style={{ borderBottom: '1px solid var(--c-b1)' }}>
          <div>
            <h2 className="text-sm font-semibold" style={{ color: 'var(--c-t1)' }}>Gestión de Usuarios</h2>
            <p className="text-xs mt-0.5" style={{ color: 'var(--c-t4)' }}>{users.length} usuarios registrados</p>
          </div>
          <button onClick={onClose} style={{ color: 'var(--c-t3)', cursor: 'pointer', background: 'none', border: 'none', padding: 4 }}
            onMouseEnter={e => (e.currentTarget.style.color = 'var(--c-t1)')}
            onMouseLeave={e => (e.currentTarget.style.color = 'var(--c-t3)')}>
            <svg viewBox="0 0 16 16" fill="currentColor" className="w-4 h-4">
              <path d="M3.72 3.72a.75.75 0 011.06 0L8 6.94l3.22-3.22a.75.75 0 111.06 1.06L9.06 8l3.22 3.22a.75.75 0 11-1.06 1.06L8 9.06l-3.22 3.22a.75.75 0 01-1.06-1.06L6.94 8 3.72 4.78a.75.75 0 010-1.06z"/>
            </svg>
          </button>
        </div>

        {/* Tabs */}
        <div className="flex" style={{ borderBottom: '1px solid var(--c-b1)' }}>
          {(['list', 'new'] as const).map(t => (
            <button key={t} onClick={() => setTab(t)}
              className="px-5 py-2.5 text-xs font-semibold transition-colors"
              style={{
                color: tab === t ? 'var(--c-blue)' : 'var(--c-t4)',
                borderBottom: tab === t ? '2px solid var(--c-blue)' : '2px solid transparent',
                marginBottom: -1, background: 'none', cursor: 'pointer',
                borderTop: 'none', borderLeft: 'none', borderRight: 'none',
              }}>
              {t === 'list' ? 'Lista de Usuarios' : 'Nuevo Usuario'}
            </button>
          ))}
        </div>

        {/* Content */}
        <div className="p-5" style={{ maxHeight: 400, overflowY: 'auto' }}>
          {tab === 'list' ? (
            <table className="w-full" style={{ borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  {['Nombre', 'Usuario', 'Rol', 'Estado', ''].map(h => (
                    <th key={h} style={{ textAlign: 'left', paddingBottom: 8, fontSize: 10, fontWeight: 700, color: 'var(--c-t4)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.id} style={{ borderTop: '1px solid var(--c-b3)' }}>
                    <td style={{ padding: '9px 0', fontSize: 12, fontWeight: 600, color: 'var(--c-t1)' }}>{u.nombre}</td>
                    <td style={{ padding: '9px 0', fontSize: 11, fontFamily: 'var(--font-mono)', color: 'var(--c-t3)' }}>{u.usuario}</td>
                    <td style={{ padding: '9px 0' }}>
                      <span style={{
                        padding: '2px 8px', borderRadius: 4, fontSize: 10, fontWeight: 700,
                        backgroundColor: u.rol === 'admin' ? 'rgba(227,179,65,0.12)' : 'rgba(47,129,247,0.12)',
                        color: u.rol === 'admin' ? 'var(--c-amber)' : 'var(--c-blue)',
                        border: `1px solid ${u.rol === 'admin' ? 'rgba(227,179,65,0.3)' : 'rgba(47,129,247,0.3)'}`,
                      }}>
                        {u.rol === 'admin' ? 'Admin' : 'Operador'}
                      </span>
                    </td>
                    <td style={{ padding: '9px 0' }}>
                      <span className="flex items-center gap-1.5" style={{ fontSize: 11, color: u.activo ? 'var(--c-green)' : 'var(--c-t4)' }}>
                        <span style={{ width: 6, height: 6, borderRadius: '50%', backgroundColor: u.activo ? 'var(--c-green)' : 'var(--c-t4)', display: 'inline-block' }} />
                        {u.activo ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td style={{ padding: '9px 0', textAlign: 'right' }}>
                      <button onClick={() => onToggle(u.id)}
                        style={{ fontSize: 11, padding: '3px 8px', borderRadius: 4, cursor: 'pointer', backgroundColor: 'var(--c-s2)', color: 'var(--c-t3)', border: '1px solid var(--c-b1)' }}
                        onMouseEnter={e => (e.currentTarget.style.color = 'var(--c-t1)')}
                        onMouseLeave={e => (e.currentTarget.style.color = 'var(--c-t3)')}>
                        {u.activo ? 'Desactivar' : 'Activar'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : success ? (
            <div className="flex flex-col items-center justify-center py-10 gap-3">
              <div className="w-10 h-10 rounded-full flex items-center justify-center"
                style={{ backgroundColor: 'var(--c-green-bg)', border: '1px solid rgba(63,185,80,0.3)' }}>
                <svg viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5" style={{ color: 'var(--c-green)' }}>
                  <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd"/>
                </svg>
              </div>
              <p style={{ fontSize: 13, fontWeight: 600, color: 'var(--c-green)' }}>Usuario creado</p>
            </div>
          ) : (
            <form onSubmit={handleAdd} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label style={{ display: 'block', fontSize: 10, fontWeight: 700, color: 'var(--c-t3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>Nombre</label>
                  <input style={inp} value={nombre} onChange={e => setNombre(e.target.value)} required placeholder="Juan Pérez"
                    onFocus={e => (e.target.style.borderColor = 'var(--c-blue)')}
                    onBlur={e =>  (e.target.style.borderColor = 'var(--c-b1)')} />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: 10, fontWeight: 700, color: 'var(--c-t3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>Usuario</label>
                  <input style={{ ...inp, fontFamily: 'var(--font-mono)' }} value={usuario} onChange={e => setUsuario(e.target.value)} required placeholder="j.perez"
                    onFocus={e => (e.target.style.borderColor = 'var(--c-blue)')}
                    onBlur={e =>  (e.target.style.borderColor = 'var(--c-b1)')} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label style={{ display: 'block', fontSize: 10, fontWeight: 700, color: 'var(--c-t3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>Rol</label>
                  <select style={{ ...inp, cursor: 'pointer' }} value={rol} onChange={e => setRol(e.target.value as 'admin' | 'operador')}>
                    <option value="operador">Operador</option>
                    <option value="admin">Administrador</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: 10, fontWeight: 700, color: 'var(--c-t3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>Turno</label>
                  <select style={{ ...inp, cursor: 'pointer' }} value={turno} onChange={e => setTurno(e.target.value)}>
                    <option value="">Seleccionar...</option>
                    <option>Turno A (06:00–18:00)</option>
                    <option>Turno B (18:00–06:00)</option>
                  </select>
                </div>
              </div>
              <div>
                <label style={{ display: 'block', fontSize: 10, fontWeight: 700, color: 'var(--c-t3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>Contraseña temporal</label>
                <input type="password" style={{ ...inp, fontFamily: 'var(--font-mono)' }}
                  value={password} onChange={e => setPassword(e.target.value)}
                  placeholder="••••••••" required
                  onFocus={e => (e.target.style.borderColor = 'var(--c-blue)')}
                  onBlur={e =>  (e.target.style.borderColor = 'var(--c-b1)')} />
                <p style={{ fontSize: 11, marginTop: 4, color: 'var(--c-t4)' }}>El usuario deberá cambiarla en su primer acceso.</p>
              </div>
              <button type="submit"
                className="w-full py-2.5 text-xs font-semibold rounded-md transition-colors"
                style={{ backgroundColor: 'var(--c-blue)', color: '#fff', border: 'none', cursor: 'pointer' }}
                onMouseEnter={e => (e.currentTarget.style.backgroundColor = 'var(--c-blue-h)')}
                onMouseLeave={e => (e.currentTarget.style.backgroundColor = 'var(--c-blue)')}>
                Crear Usuario
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
