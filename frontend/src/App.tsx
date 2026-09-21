import { useState, useEffect } from 'react';
import type { ViewType, UserRole, AuditEntry, AppUser } from './types';
import { initialAuditLog } from './data/mockData';
import { apiLogin, apiGetCurrentUser, apiLogout } from './api/client';
import LoginView from './views/LoginView';
import AdminView from './views/AdminView';
import OperatorView from './views/OperatorView';

export default function App() {
  const [view, setView] = useState<ViewType>('login');
  const [userRole, setUserRole] = useState<UserRole>('operador');
  const [userName, setUserName] = useState('');
  const [currentUser, setCurrentUser] = useState<AppUser | null>(null);
  const [auditLog, setAuditLog] = useState<AuditEntry[]>(initialAuditLog);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    async function restoreSession() {
      try {
        const user = await apiGetCurrentUser();
        if (user && user.usuario) {
          setCurrentUser(user);
          setUserName(user.nombre);
          setUserRole(user.rol as UserRole);
          setView(user.rol === 'admin' ? 'admin' : 'operator');
        }
      } catch {
        // No session or token expired - stay on login
      } finally {
        setInitializing(false);
      }
    }
    restoreSession();
  }, []);

  const handleLogin = async (username: string, password: string): Promise<string | null> => {
    try {
      const { user } = await apiLogin(username, password);
      setCurrentUser(user);
      setUserName(user.nombre);
      setUserRole(user.rol as UserRole);
      setView(user.rol === 'admin' ? 'admin' : 'operator');
      return null;
    } catch (err: any) {
      return err.message || 'Error de autenticación. Verifique sus credenciales.';
    }
  };

  const handleLogout = () => {
    apiLogout();
    setView('login');
    setUserName('');
    setCurrentUser(null);
  };

  const addAuditEntry = (entry: Omit<AuditEntry, 'id'>) => {
    setAuditLog(prev => [{ ...entry, id: Date.now() }, ...prev]);
  };

  if (initializing) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--c-bg)] text-[var(--c-t3)] text-xs font-mono">
        Iniciando sistema de bitácora...
      </div>
    );
  }

  if (view === 'login') {
    return <LoginView onLogin={handleLogin} />;
  }
  if (view === 'admin') {
    return <AdminView userName={userName} onLogout={handleLogout} auditLog={auditLog} />;
  }
  return (
    <OperatorView
      userName={userName}
      currentUser={currentUser}
      onLogout={handleLogout}
      addAuditEntry={addAuditEntry}
    />
  );
}

