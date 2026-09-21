import { useState, useEffect, useRef } from 'react';
import type { PendingEdit } from '../types';
import { fmtHour } from '../data/mockData';

interface Props {
  pending: PendingEdit;
  onConfirm: (justificacion: string) => void;
  onCancel: () => void;
}

export default function JustifyModal({ pending, onConfirm, onCancel }: Props) {
  const [text, setText] = useState('');
  const ref = useRef<HTMLTextAreaElement>(null);

  useEffect(() => { ref.current?.focus(); }, []);

  const ok = text.trim().length >= 10;

  return (
    <div className="modal-backdrop fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ backgroundColor: 'rgba(13,17,23,0.85)', backdropFilter: 'blur(4px)' }}
      onClick={e => { if (e.target === e.currentTarget) onCancel(); }}>

      <div className="modal-card w-full max-w-md rounded-xl"
        style={{ backgroundColor: 'var(--c-s1)', border: '1px solid var(--c-b2)', boxShadow: '0 24px 64px rgba(0,0,0,0.55)' }}>

        {/* Header */}
        <div className="flex items-center gap-3 px-5 py-4"
          style={{ borderBottom: '1px solid var(--c-b1)' }}>
          <div className="w-8 h-8 rounded-lg flex items-center justify-center"
            style={{ backgroundColor: 'rgba(227,179,65,0.12)', border: '1px solid rgba(227,179,65,0.3)' }}>
            <svg viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4" style={{ color: 'var(--c-amber)' }}>
              <path fillRule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd"/>
            </svg>
          </div>
          <div>
            <h2 className="text-sm font-semibold" style={{ color: 'var(--c-t1)' }}>Justificar Cambio</h2>
            <p className="text-xs" style={{ color: 'var(--c-t4)' }}>Registro obligatorio de auditoría</p>
          </div>
        </div>

        {/* Change info */}
        <div className="mx-5 my-4 rounded-lg px-4 py-3 text-xs font-mono"
          style={{ backgroundColor: 'var(--c-bg)', border: '1px solid var(--c-b1)' }}>
          <div className="grid grid-cols-2 gap-y-2">
            {[
              ['Hora afectada', fmtHour(pending.hour), 'var(--c-blue-l)'],
              ['Campo', pending.fieldLabel, 'var(--c-t2)'],
              ['Valor anterior', pending.oldValue || '—', 'var(--c-red)'],
              ['Valor nuevo', pending.newValue, 'var(--c-green)'],
            ].map(([label, val, color]) => (
              <>
                <span key={label + 'l'} style={{ color: 'var(--c-t4)' }}>{label}</span>
                <span key={label + 'v'} className="text-right" style={{ color: color as string }}>{val}</span>
              </>
            ))}
          </div>
        </div>

        {/* Textarea */}
        <div className="px-5 pb-5">
          <label className="block text-xs font-semibold uppercase tracking-widest mb-2"
            style={{ color: 'var(--c-t3)' }}>Motivo del cambio *</label>
          <textarea ref={ref} value={text} onChange={e => setText(e.target.value)} rows={3}
            placeholder="Describa con precisión el motivo de esta corrección..."
            className="w-full text-xs rounded-lg px-3 py-2.5 resize-none"
            style={{
              backgroundColor: 'var(--c-bg)',
              border: '1px solid var(--c-b1)',
              color: 'var(--c-t1)',
              fontFamily: 'var(--font-sans)',
              outline: 'none',
            }}
            onFocus={e => (e.target.style.borderColor = 'var(--c-blue)')}
            onBlur={e =>  (e.target.style.borderColor = 'var(--c-b1)')} />
          {text.length > 0 && !ok && (
            <p className="text-xs mt-1.5" style={{ color: 'var(--c-amber)' }}>Mínimo 10 caracteres.</p>
          )}

          <div className="flex gap-3 mt-4">
            <button onClick={onCancel}
              className="flex-1 py-2 text-xs font-semibold rounded-md transition-colors"
              style={{ backgroundColor: 'var(--c-s2)', color: 'var(--c-t3)', border: '1px solid var(--c-b1)', cursor: 'pointer' }}
              onMouseEnter={e => (e.currentTarget.style.backgroundColor = 'var(--c-s3)')}
              onMouseLeave={e => (e.currentTarget.style.backgroundColor = 'var(--c-s2)')}>
              Cancelar
            </button>
            <button onClick={() => ok && onConfirm(text.trim())} disabled={!ok}
              className="flex-1 py-2 text-xs font-semibold rounded-md transition-colors"
              style={{
                backgroundColor: ok ? 'var(--c-blue)' : 'var(--c-s2)',
                color: ok ? '#fff' : 'var(--c-t5)',
                border: 'none',
                cursor: ok ? 'pointer' : 'not-allowed',
              }}
              onMouseEnter={e => { if (ok) e.currentTarget.style.backgroundColor = 'var(--c-blue-h)'; }}
              onMouseLeave={e => { if (ok) e.currentTarget.style.backgroundColor = 'var(--c-blue)'; }}>
              Confirmar y Guardar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
