import React, { useEffect, useId, useState } from 'react';
import { AlertCircle, Trash2, X } from 'lucide-react';

/* ─── Sheet ───────────────────────────────────────────────────────────────────
   The one bottom-sheet shell for every editor: scrim, handle, title, close,
   Escape to dismiss and body scroll lock. Content scrolls; the footer stays. */

interface SheetProps {
  open: boolean;
  title: string;
  onClose: () => void;
  onSubmit?: (e: React.FormEvent) => void;
  footer?: React.ReactNode;
  children: React.ReactNode;
  /** Blocks dismissal by scrim/Escape (used by first-run setup). */
  dismissible?: boolean;
}

export const Sheet: React.FC<SheetProps> = ({ open, title, onClose, onSubmit, footer, children, dismissible = true }) => {
  const titleId = useId();

  useEffect(() => {
    if (!open) return;
    const original = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && dismissible) onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => {
      document.body.style.overflow = original;
      window.removeEventListener('keydown', onKey);
    };
  }, [open, onClose, dismissible]);

  if (!open) return null;

  const Container = onSubmit ? 'form' : 'div';

  return (
    <div className="scrim" onClick={dismissible ? onClose : undefined}>
      <Container
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className="sheet"
        onClick={(e: React.MouseEvent) => e.stopPropagation()}
        onSubmit={onSubmit}
      >
        <div className="sheet-handle" aria-hidden="true" />
        <div className="sheet-head">
          <h2 id={titleId} className="sheet-title">{title}</h2>
          {dismissible && (
            <button type="button" className="icon-btn icon-btn-filled" aria-label="Close" onClick={onClose}>
              <X size={18} />
            </button>
          )}
        </div>
        <div className="sheet-body scroll-container">{children}</div>
        {footer && <div className="sheet-foot">{footer}</div>}
      </Container>
    </div>
  );
};

/* ─── Field ───────────────────────────────────────────────────────────────── */

interface FieldProps {
  label: string;
  hint?: string;
  children: React.ReactElement<{ id?: string }>;
  className?: string;
}

/** Label above, control, optional hint below. Wires the label to the control. */
export const Field: React.FC<FieldProps> = ({ label, hint, children, className }) => {
  const id = useId();
  return (
    <div className={`field ${className ?? ''}`}>
      <label htmlFor={id} className="field-label">{label}</label>
      {React.cloneElement(children, { id })}
      {hint && <span className="field-hint">{hint}</span>}
    </div>
  );
};

/** For groups of buttons (chips, segmented) where a <label for> cannot apply. */
export const FieldGroup: React.FC<{ label: string; hint?: string; children: React.ReactNode }> = ({ label, hint, children }) => {
  const id = useId();
  return (
    <div className="field" role="group" aria-labelledby={id}>
      <span id={id} className="field-label">{label}</span>
      {children}
      {hint && <span className="field-hint">{hint}</span>}
    </div>
  );
};

export const FormError: React.FC<{ message: string }> = ({ message }) =>
  message ? (
    <div className="alert" role="alert">
      <AlertCircle size={16} style={{ flexShrink: 0, marginTop: 1 }} />
      <span>{message}</span>
    </div>
  ) : null;

/* ─── Segmented control ───────────────────────────────────────────────────── */

interface SegmentedProps<T extends string> {
  label: string;
  value: T;
  options: Array<{ id: T; label: string; icon?: React.ComponentType<{ size?: number }> }>;
  onChange: (value: T) => void;
}

export function Segmented<T extends string>({ label, value, options, onChange }: SegmentedProps<T>) {
  return (
    <div className="segmented" role="radiogroup" aria-label={label}>
      {options.map(opt => {
        const Icon = opt.icon;
        return (
          <button
            key={opt.id}
            type="button"
            role="radio"
            aria-checked={value === opt.id}
            onClick={() => onChange(opt.id)}
          >
            {Icon && <Icon size={14} />}
            {opt.label}
          </button>
        );
      })}
    </div>
  );
}

/* ─── Section header ──────────────────────────────────────────────────────── */

interface SectionHeadProps {
  title: string;
  count?: number;
  action?: React.ReactNode;
  id?: string;
}

export const SectionHead: React.FC<SectionHeadProps> = ({ title, count, action, id }) => (
  <div className="section-head">
    <h3 id={id} className="section-title">
      {title}
      {count != null && <span className="section-count">{count}</span>}
    </h3>
    {action}
  </div>
);

/* ─── Empty state ─────────────────────────────────────────────────────────── */

interface EmptyStateProps {
  title: string;
  body?: string;
  action?: React.ReactNode;
}

export const EmptyState: React.FC<EmptyStateProps> = ({ title, body, action }) => (
  <div className="empty">
    <div className="stack-4">
      <div className="empty-title">{title}</div>
      {body && <p className="empty-body">{body}</p>}
    </div>
    {action}
  </div>
);

/* ─── Two-step delete for sheet footers ───────────────────────────────────── */

interface DeleteConfirmProps {
  label: string;
  confirmText: string;
  onConfirm: () => void;
  disabled?: boolean;
}

export const DeleteConfirm: React.FC<DeleteConfirmProps> = ({ label, confirmText, onConfirm, disabled }) => {
  const [asking, setAsking] = useState(false);

  if (!asking) {
    return (
      <button type="button" className="btn btn-lg btn-danger-quiet" onClick={() => setAsking(true)} disabled={disabled}>
        <Trash2 size={16} />
        {label}
      </button>
    );
  }

  return (
    <div className="panel stack-12" style={{ padding: 14, background: 'var(--danger-soft)', animation: 'pop 0.18s var(--ease-out) both' }}>
      <p style={{ fontSize: 13.5, color: 'var(--text)', lineHeight: 1.5 }}>{confirmText}</p>
      <div className="form-grid-2">
        <button type="button" className="btn btn-secondary" onClick={() => setAsking(false)}>Keep it</button>
        <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={disabled}>Delete</button>
      </div>
    </div>
  );
};
