import React, { useEffect, useRef, useState } from 'react';
import { Bell, Check, Clock, Link as LinkIcon } from 'lucide-react';
import { type Reminder } from '../lib/db';
import { useTasks } from '../context/TasksContext';
import { useNow } from '../lib/datetime';

interface ReminderCardProps {
  reminder: Reminder;
  onDismiss: (id: string) => void;
  onSnooze: (id: string, newTimeEpochMs: number) => void;
  onEdit: (reminder: Reminder) => void;
}

const SNOOZE_OPTIONS: Array<{ label: string; at: () => number }> = [
  { label: 'In 10 minutes', at: () => Date.now() + 10 * 60000 },
  { label: 'In 30 minutes', at: () => Date.now() + 30 * 60000 },
  { label: 'In 1 hour', at: () => Date.now() + 60 * 60000 },
  {
    label: 'Tomorrow, 9 AM',
    at: () => {
      const d = new Date();
      d.setDate(d.getDate() + 1);
      d.setHours(9, 0, 0, 0);
      return d.getTime();
    }
  }
];

/** One reminder as a row; place inside a `.list` surface. */
export const ReminderCard: React.FC<ReminderCardProps> = ({ reminder, onDismiss, onSnooze, onEdit }) => {
  const [showSnooze, setShowSnooze] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const { tasks } = useTasks();
  // Ticks so "5m late" and the due-soon tint stay accurate without a reload.
  const now = useNow(30000);

  const linkedTask = reminder.taskId ? tasks.find(t => t.id === reminder.taskId) : null;
  const isLate = reminder.reminderAt < now;
  const isSoon = !isLate && reminder.reminderAt - now < 2 * 3600000;

  useEffect(() => {
    if (!showSnooze) return;
    const close = (e: MouseEvent | TouchEvent) => {
      if (!menuRef.current?.contains(e.target as Node)) setShowSnooze(false);
    };
    document.addEventListener('mousedown', close);
    document.addEventListener('touchstart', close);
    return () => {
      document.removeEventListener('mousedown', close);
      document.removeEventListener('touchstart', close);
    };
  }, [showSnooze]);

  const when = (() => {
    const d = new Date(reminder.reminderAt);
    const time = d.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
    if (isLate) {
      const mins = Math.max(1, Math.round((now - reminder.reminderAt) / 60000));
      return mins < 60 ? `${mins}m late` : `Was ${time}`;
    }
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    if (d.toDateString() === new Date().toDateString()) return `Today ${time}`;
    if (d.toDateString() === tomorrow.toDateString()) return `Tomorrow ${time}`;
    return `${d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} ${time}`;
  })();

  return (
    <div
      role="button"
      tabIndex={0}
      className="list-row"
      onClick={() => onEdit(reminder)}
      onKeyDown={(e) => {
        if (e.key === 'Enter') onEdit(reminder);
      }}
      style={{ cursor: 'pointer', position: 'relative', zIndex: showSnooze ? 5 : undefined, overflow: 'visible' }}
    >
      <span
        className="row"
        style={{
          justifyContent: 'center',
          width: 34,
          height: 34,
          borderRadius: 'var(--r-control)',
          background: isLate ? 'var(--danger-soft)' : isSoon ? 'var(--warn-soft)' : 'var(--surface-2)',
          color: isLate ? 'var(--danger)' : isSoon ? 'var(--warn)' : 'var(--text-2)',
          flexShrink: 0
        }}
      >
        <Bell size={17} />
      </span>

      <div className="grow">
        <div className="row-title truncate">{reminder.title}</div>
        <div className="row" style={{ gap: 10, marginTop: 2 }}>
          <span
            className="meta mono"
            style={{ fontSize: 12, color: isLate ? 'var(--danger)' : isSoon ? 'var(--warn)' : 'var(--text-3)', fontWeight: 550 }}
          >
            {when}
          </span>
          {linkedTask && (
            <span className="row meta truncate" style={{ gap: 4, minWidth: 0 }}>
              <LinkIcon size={11} style={{ flexShrink: 0 }} />
              <span className="truncate">{linkedTask.title}</span>
            </span>
          )}
        </div>
      </div>

      <div ref={menuRef} style={{ position: 'relative', flexShrink: 0 }}>
        <button
          type="button"
          aria-label={`Snooze "${reminder.title}"`}
          aria-haspopup="menu"
          aria-expanded={showSnooze}
          className="icon-btn"
          onClick={(e) => {
            e.stopPropagation();
            setShowSnooze(v => !v);
          }}
        >
          <Clock size={18} />
        </button>
        {showSnooze && (
          <div
            role="menu"
            className="list"
            onClick={(e) => e.stopPropagation()}
            style={{
              position: 'absolute',
              right: 0,
              top: 40,
              width: 180,
              zIndex: 70,
              boxShadow: 'var(--shadow-2)',
              animation: 'pop 0.16s var(--ease-out) both'
            }}
          >
            {SNOOZE_OPTIONS.map(opt => (
              <button
                key={opt.label}
                type="button"
                role="menuitem"
                className="list-row"
                style={{ minHeight: 44, fontSize: 14 }}
                onClick={() => {
                  setShowSnooze(false);
                  onSnooze(reminder.id, opt.at());
                }}
              >
                {opt.label}
              </button>
            ))}
          </div>
        )}
      </div>

      <button
        type="button"
        aria-label={`Mark "${reminder.title}" done`}
        className="icon-btn"
        style={{ color: 'var(--ok)' }}
        onClick={(e) => {
          e.stopPropagation();
          onDismiss(reminder.id);
        }}
      >
        <Check size={19} />
      </button>
    </div>
  );
};
