import { useState } from 'react';

interface Props {
  onLogin: (username: string, password: string) => Promise<string | null> | string | null;
}

export default function LoginView({ onLogin }: Props) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]   = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const err = await onLogin(username, password);
      if (err) setError(err);
    } catch (err: any) {
      setError(err.message || 'Error de conexión con el servidor');
    } finally {
      setLoading(false);
    }
  };

  const field: React.CSSProperties = {
    width: '100%',
    backgroundColor: 'var(--c-bg)',
    border: '1px solid var(--c-b1)',
    borderRadius: 6,
    color: 'var(--c-t1)',
    fontSize: 13,
    padding: '8px 12px',
    outline: 'none',
    fontFamily: 'var(--font-mono)',
    transition: 'border-color .15s, box-shadow .15s',
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 topo-bg"
      style={{ backgroundColor: 'var(--c-bg)' }}>

      {/* radial glow */}
      <div className="absolute inset-0 pointer-events-none"
        style={{ background: 'radial-gradient(ellipse 55% 40% at 50% 35%, rgba(47,129,247,0.06) 0%, transparent 70%)' }} />

      <div className="relative z-10 w-full max-w-[380px]">

        {/* Logo block */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl mb-5"
            style={{ backgroundColor: 'var(--c-s2)', border: '1px solid var(--c-b1)', boxShadow: '0 0 24px rgba(47,129,247,0.12)' }}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" className="w-7 h-7" style={{ color: 'var(--c-blue)' }}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
            </svg>
          </div>
          <h1 className="text-xl font-bold tracking-tight" style={{ color: 'var(--c-t1)', letterSpacing: '-0.02em' }}>
            Bitácora Hidroeléctrica
          </h1>
          <p className="text-xs mt-1.5 font-mono uppercase tracking-widest" style={{ color: 'var(--c-t4)', letterSpacing: '0.08em' }}>
            Sistema de Registro · v2.1.4
          </p>
        </div>

        {/* Card */}
        <div className="rounded-xl p-7"
          style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b1)', boxShadow: '0 8px 32px rgba(0,0,0,0.4)' }}>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest mb-2"
                style={{ color: 'var(--c-t3)' }}>Usuario</label>
              <input type="text" value={username} onChange={e => setUsername(e.target.value)}
                autoComplete="username" required placeholder="nombre.usuario"
                style={field}
                onFocus={e => { e.target.style.borderColor = 'var(--c-blue)'; e.target.style.boxShadow = '0 0 0 3px rgba(47,129,247,0.12)'; }}
                onBlur={e =>  { e.target.style.borderColor = 'var(--c-b1)';   e.target.style.boxShadow = 'none'; }} />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-widest mb-2"
                style={{ color: 'var(--c-t3)' }}>Contraseña</label>
              <input type="password" value={password} onChange={e => setPassword(e.target.value)}
                autoComplete="current-password" required placeholder="••••••••"
                style={field}
                onFocus={e => { e.target.style.borderColor = 'var(--c-blue)'; e.target.style.boxShadow = '0 0 0 3px rgba(47,129,247,0.12)'; }}
                onBlur={e =>  { e.target.style.borderColor = 'var(--c-b1)';   e.target.style.boxShadow = 'none'; }} />
            </div>

            {error && (
              <div className="flex items-start gap-2.5 rounded-md px-3 py-2.5 text-xs"
                style={{ backgroundColor: 'var(--c-red-bg)', border: '1px solid rgba(248,81,73,0.35)', color: 'var(--c-red)' }}>
                <svg viewBox="0 0 16 16" fill="currentColor" className="w-3.5 h-3.5 mt-0.5 shrink-0">
                  <path d="M8 1a7 7 0 100 14A7 7 0 008 1zm-.75 3.75a.75.75 0 011.5 0v3.5a.75.75 0 01-1.5 0v-3.5zm.75 7.25a1 1 0 110-2 1 1 0 010 2z" />
                </svg>
                {error}
              </div>
            )}

            <button type="submit" disabled={loading}
              className="w-full rounded-md py-2.5 text-sm font-semibold mt-1 transition-colors"
              style={{ backgroundColor: 'var(--c-blue)', color: '#fff', border: 'none', cursor: loading ? 'not-allowed' : 'pointer', opacity: loading ? 0.75 : 1 }}
              onMouseEnter={e => { if (!loading) (e.currentTarget.style.backgroundColor = 'var(--c-blue-h)'); }}
              onMouseLeave={e => { (e.currentTarget.style.backgroundColor = 'var(--c-blue)'); }}>
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin w-3.5 h-3.5" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" strokeOpacity="0.2" />
                    <path d="M12 2a10 10 0 0110 10" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
                  </svg>
                  Verificando...
                </span>
              ) : 'Iniciar Sesión'}
            </button>
          </form>

          <div className="mt-5 pt-4 text-center" style={{ borderTop: '1px solid var(--c-b3)' }}>
            <p className="text-xs font-mono" style={{ color: 'var(--c-t4)' }}>
              demo:{' '}
              <span className="cursor-pointer transition-colors" style={{ color: 'var(--c-t3)' }}
                onMouseEnter={e => (e.currentTarget.style.color = 'var(--c-blue-l)')}
                onMouseLeave={e => (e.currentTarget.style.color = 'var(--c-t3)')}
                onClick={() => { setUsername('admin'); setPassword('admin123'); }}>
                admin/admin123
              </span>
              {' · '}
              <span className="cursor-pointer transition-colors" style={{ color: 'var(--c-t3)' }}
                onMouseEnter={e => (e.currentTarget.style.color = 'var(--c-blue-l)')}
                onMouseLeave={e => (e.currentTarget.style.color = 'var(--c-t3)')}
                onClick={() => { setUsername('c.mendoza'); setPassword('op123'); }}>
                c.mendoza/op123
              </span>
            </p>
          </div>
        </div>

        <p className="text-center text-xs mt-5 font-mono" style={{ color: 'var(--c-t5)' }}>
          Central Hidroeléctrica del Río Cauca
        </p>
      </div>
    </div>
  );
}
